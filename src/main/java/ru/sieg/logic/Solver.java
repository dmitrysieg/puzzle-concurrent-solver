package ru.sieg.logic;

import net.sf.image4j.codec.bmp.BMPEncoder;
import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Plane;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;
import ru.sieg.view.MainView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Solver {

    private final List<PiecesCluster> piecesClusters;
    private final Random random;
    private final SolverCompany solverCompany;
    private final String name;
    private final MainView view;

    private PieceRepository pieceRepository;
    private boolean boredState;
    private PiecesCluster clusterToConsider;
    private int ownedPieces;

    public Solver(final String name,
                  final SolverCompany solverCompany,
                  final MainView view) {
        this.piecesClusters = new ArrayList<>();
        this.solverCompany = solverCompany;
        this.name = name;
        this.view = view;
        this.random = new Random(System.currentTimeMillis());
    }

    public List<PiecesCluster> getPiecesClusters() {
        return piecesClusters;
    }

    public int getPiecesClustersSize() {
        return piecesClusters.size();
    }

    public SolverCompany getSolverCompany() {
        return solverCompany;
    }

    public String getName() {
        return name;
    }

    public Collection<Solver> getOtherSolvers() {
        return getSolverCompany().getSolvers().stream()
                .filter(s -> !s.equals(this))
                .collect(Collectors.toList());
    }

    public Optional<PiecesCluster> getMaxCluster() {
        return piecesClusters.stream().max(Comparator.comparingInt(o -> o.piecesMap.size()));
    }

    public int getOwnedPieces() {
        return ownedPieces;
    }

    @Override
    public String toString() {
        final String clustersSizeString = getPiecesClusters().stream().map(PiecesCluster::size).map(String::valueOf).collect(Collectors.joining(" "));
        return new StringBuilder(String.valueOf(getPiecesClusters().size()))
                .append(" (")
                .append(clustersSizeString)
                .append(")")
                .toString();
    }

    public String getPiecesAsStr() {
        return getSolverCompany().getSolvers().stream()
                .map(Solver::toString)
                .collect(Collectors.joining(" ")) + " (=" + pieceRepository.size() + ")";
    }

    public void pointToRepository(final PieceRepository pieceRepository) {
        this.pieceRepository = pieceRepository;
//        try {
//            Thread.sleep(random.nextInt(500));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        this.start();
    }

    public void start(final MainView view) {
        if (pieceRepository == null) {
            throw new IllegalStateException("pieceRepository is not set");
        }
        while (!pieceRepository.isEmpty()) {
            doPieceApproach(view);
        }
        // log
    }

    public void doPieceApproach(final MainView view) {

        long time = System.currentTimeMillis();

        clusterToConsider = doSelectCluster();
        if (clusterToConsider == null) {
            return;
        }

        // do process cluster
        // Strategy:
        // 1) select random element of the cluster, connected with air;
        // 2) find an element in the repo, matching with the profile of the selected element;
        // 3) connect;

        final Optional<Map.Entry<Point, Piece>> _airedPieceEntry = clusterToConsider.getRandomAired();
        if (!_airedPieceEntry.isPresent()) {
            throw new IllegalStateException("Can't obtain any aired piece");
        }
        final Map.Entry<Point, Piece> airedPieceEntry = _airedPieceEntry.get();

        final Side airedSide = clusterToConsider.getRandomAiredSide(airedPieceEntry);
        final Point socket = airedPieceEntry.getKey().getBy(airedSide);

        final Side sideToFind = airedSide.getComplimentary();
        final Profile searchedProfile = airedPieceEntry.getValue().getProfile(airedSide).getComplementaryProfile();

        final Optional<Piece> matched = pieceRepository.findPiece(sideToFind, searchedProfile);

        if (matched.isPresent()) {
            final Piece piece = matched.get();
            clusterToConsider.piecesMap.put(socket, pieceRepository.pop(piece));

            view.update(this);
            System.out.println(getPiecesAsStr());

        } else {
            // искать сначала в своих кластерах, потом только в чужих.
            Optional<ConfluenceInfo> attemptToFindOwnClusters = askForCluster(searchedProfile, sideToFind, clusterToConsider);
            if (attemptToFindOwnClusters.isPresent()) {

                final ConfluenceInfo foundClusterInfo = attemptToFindOwnClusters.get();

                final PiecesCluster myCluster = clusterToConsider;
                this.piecesClusters.remove(clusterToConsider);
                this.piecesClusters.remove(foundClusterInfo.getCluster());

                final PiecesCluster resultCluster = mergeClusters(
                        myCluster,
                        foundClusterInfo.getCluster(),
                        airedPieceEntry.getKey(),
                        foundClusterInfo.getPoint(),
                        airedSide
                );

                piecesClusters.add(resultCluster);
                clusterToConsider = resultCluster;

                view.update(this);
                System.out.println(getPiecesAsStr());
            } else {
                // try to obtain from other solvers

                long time2 = System.currentTimeMillis();

                boolean obtainedFromSolvers = false;

                for (final Solver solver : getOtherSolvers()) {

                    final Optional<ConfluenceInfo> attemptedToFind = solver.askForCluster(searchedProfile, sideToFind);

                    if (attemptedToFind.isPresent()) {

                        final ConfluenceInfo foundClusterInfo = attemptedToFind.get();

                        final PiecesCluster myCluster = clusterToConsider;
                        this.piecesClusters.remove(clusterToConsider);
                        clusterToConsider = null;

                        // too much intervention todo
                        solver.piecesClusters.remove(foundClusterInfo.getCluster());
                        if (solver.clusterToConsider == null) {
//                            System.out.println("solver.clusterToConsider == null");
                        }
                        if (foundClusterInfo.getCluster() != null && foundClusterInfo.getCluster().equals(solver.clusterToConsider)) {
                            solver.clusterToConsider = null;
                        }

                        final boolean forMe = arbitrageWhoTakesCluster(myCluster, foundClusterInfo.getCluster(), random);
                        final Solver whoTakes = forMe ? this : solver;

                        final PiecesCluster resultCluster = mergeClusters(
                                myCluster,
                                foundClusterInfo.getCluster(),
                                airedPieceEntry.getKey(),
                                foundClusterInfo.getPoint(),
                                airedSide
                        );
                        whoTakes.getPiecesClusters().add(resultCluster);
                        whoTakes.clusterToConsider = resultCluster;
                        //view.update(this);
                        //view.update(solver);
                        System.out.println(getPiecesAsStr());

                        obtainedFromSolvers = true;

                        break;
                    }
                }
                System.out.println(this.getName() + "\t\tdoPA(): askForClusters(): " + (System.currentTimeMillis() - time2) + " ms");
                if (!obtainedFromSolvers) {
                    //System.out.println(piecesClusters.get(0).toImage64());
                    throw new IllegalStateException("Can't either find a piece in repo, or ask any from other solvers");
                }
            }
        }

        // set to bored - strategy can vary todo
        if (random.nextInt(1000) > 997) {
            boredState = true;
            System.out.println(this.getName() + "\t\tGot bored!");
        }

        ownedPieces = recalculateOwnedPieces();

        final long tt = (System.currentTimeMillis() - time);
        if (tt < 20) {return;}
        System.out.println(this.getName() + "\t\tdoPieceApproach(): " + tt + " ms");
    }

    private PiecesCluster doSelectCluster() {

        if (clusterToConsider == null || boredState) {

            final Optional<Piece> newPiece = pieceRepository.popRandomPiece();

            if (newPiece.isPresent()) {
                final PiecesCluster result = new PiecesCluster(newPiece.get());
                piecesClusters.add(result);
                boredState = false;
                return result;
            }
            if (!piecesClusters.isEmpty()) {
                return piecesClusters.get(random.nextInt(piecesClusters.size()));
            }
            System.out.println(this.getName() + "\t\tNothing to pick up.");
            return null;
        }

        return piecesClusters.get(random.nextInt(piecesClusters.size()));
    }

    private int recalculateOwnedPieces() {
        return piecesClusters.stream()
                .map(PiecesCluster::size)
                .reduce((a, b) -> a + b)
                .orElse(0);
    }

    public Optional<ConfluenceInfo> askForCluster(final Profile refProfile, final Side refSide) {
        return piecesClusters.stream()
                .flatMap(cluster -> cluster.piecesMap.entrySet().stream().map(
                        pieceEntry -> new ConfluenceInfo(cluster, pieceEntry.getKey(), pieceEntry.getValue())
                ))
                .filter(potentialConfluence -> potentialConfluence.getPiece().getProfile(refSide).compareTo(refProfile) == 0)
                .findFirst();
    }

    public Optional<ConfluenceInfo> askForCluster(final Profile refProfile,
                                                  final Side refSide,
                                                  final PiecesCluster exclude) {
        return piecesClusters.stream()
                .filter(c -> !c.equals(exclude))
                .flatMap(cluster -> cluster.piecesMap.entrySet().stream().map(
                        pieceEntry -> new ConfluenceInfo(cluster, pieceEntry.getKey(), pieceEntry.getValue())
                ))
                .filter(potentialConfluence -> potentialConfluence.getPiece().getProfile(refSide).compareTo(refProfile) == 0)
                .findFirst();
    }

    /**
     * Moving pieces from given to mine cluster, using 2 points and 1 side as related coords.
     * Take given[confluencePoint + P] -> Move to mine[socketPoint + by(socketSide) + P]
     * @param mine
     * @param given
     * @param socketPoint
     * @param confluencePoint
     * @param socketSide
     * @return
     */
    public PiecesCluster mergeClusters(final PiecesCluster mine,
                              final PiecesCluster given,
                              final Point socketPoint,
                              final Point confluencePoint,
                              final Side socketSide) {

        long time = System.currentTimeMillis();

        //System.out.println(mine.toImageMerge64(given, socketPoint, confluencePoint, socketSide));

        final BufferedImage mergedImage = mine.toImageMerge(given, socketPoint, confluencePoint, socketSide);

        final Point sideShift = socketPoint.getBy(socketSide);
        final Point shift = Point.of(
                - confluencePoint.x + sideShift.x,
                - confluencePoint.y + sideShift.y
        );

        long time2 = System.currentTimeMillis();
        final List<Map.Entry<Point, Piece>> givenEntries = new ArrayList<>(given.piecesMap.entrySet());
        System.out.println(this.getName() + "\t\tmergeClusters(): new ArrayList(): " + (System.currentTimeMillis() - time2) + " ms");
        for (final Map.Entry<Point, Piece> entry : givenEntries) {

            final Point shifted = Point.of(
                    entry.getKey().x + shift.x,
                    entry.getKey().y + shift.y
            );

            if (mine.piecesMap.containsKey(shifted)) {
                throw new IllegalStateException("Mine cluster already contains piece in merged cluster");
            }
            mine.piecesMap.put(shifted, entry.getValue());
            given.piecesMap.remove(entry.getKey());
        }

        System.out.println(this.getName() + "\t\tmergeClusters(): " + (System.currentTimeMillis() - time) + " ms");
        return mine;
    }

    private static boolean arbitrageWhoTakesCluster(final PiecesCluster mine,
                                                    final PiecesCluster given,
                                                    final Random random) {
        return mine.size() > given.size();
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Solver) || this.getName() == null) {
            return false;
        }
        return this.getName().equals(((Solver) obj).getName());
    }

    // -------------------------------

    static class Point {

        public static final Point ZERO = of(0,0);
        static final Point[] INCRS = new Point[]{
                of( 0, -1),
                of( 1,  0),
                of( 0,  1),
                of(-1,  0)
        };

        private final int x, y;

        Point(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public Point getBy(final Side side) {
            final Point incr = INCRS[side.ordinal()];
            return of(x + incr.x, y + incr.y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x &&
                    y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        public static Point of(final int x, final int y) {
            return new Point(x, y);
        }
    }

    public static class PiecesCluster {

        public static final int IMG_SIZE = 2;

        private final Map<Point, Piece> piecesMap;
        private final Random random;

        PiecesCluster(final Piece first) {
            piecesMap = new ConcurrentHashMap<>();
            piecesMap.put(Point.ZERO, first);

            random = new Random(System.currentTimeMillis());
        }

        public int size() {
            return piecesMap.size();
        }

        public Rectangle getBoundingBox() {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE,
                    minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (Point p : piecesMap.keySet()) {
                minX = p.x < minX ? p.x : minX;
                maxX = p.x > maxX ? p.x : maxX;
                minY = p.y < minY ? p.y : minY;
                maxY = p.y > maxY ? p.y : maxY;
            }
            return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }

        public Piece getFirst() {
            return piecesMap.get(Point.ZERO);
        }

        boolean isAired(final Point point, final Side side) {
            final Point shifted = point.getBy(side);
            return !piecesMap.containsKey(shifted) && !piecesMap.get(point).isEdge(side);
        }

        Optional<Map.Entry<Point, Piece>> getRandomAired() {
            final List<Map.Entry<Point, Piece>> list = piecesMap.entrySet().stream()
                    .filter(entry -> {
                        final Point coord = entry.getKey();
                        return isAired(coord, Side.WEST) ||
                                isAired(coord, Side.EAST) ||
                                isAired(coord, Side.NORTH) ||
                                isAired(coord, Side.SOUTH);
                    })
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                return Optional.empty();
            }
            Collections.shuffle(list, random);
            return Optional.of(list.get(random.nextInt(list.size())));
        }

        Side getRandomAiredSide(final Map.Entry<Point, Piece> pieceEntry) {

            final List<Side> list = new ArrayList<>();
            final Point coord = pieceEntry.getKey();

            if (isAired(coord, Side.WEST)) {
                list.add(Side.WEST);
            }
            if (isAired(coord, Side.EAST)) {
                list.add(Side.EAST);
            }
            if (isAired(coord, Side.NORTH)) {
                list.add(Side.NORTH);
            }
            if (isAired(coord, Side.SOUTH)) {
                list.add(Side.SOUTH);
            }

            if (list.isEmpty()) {
                throw new IllegalStateException("Can't obtain an aired side, piece=" + pieceEntry);
            }
            return list.get(random.nextInt(list.size()));
        }

        @Override
        public String toString() {
            return "PiecesCluster{" + piecesMap.size() + " elements}";
        }

        public BufferedImage toImage() {
            int fromX = Integer.MAX_VALUE,
                    toX = Integer.MIN_VALUE,
                    fromY = Integer.MAX_VALUE,
                    toY = Integer.MIN_VALUE;
            for (final Point p : piecesMap.keySet()) {
                fromX = fromX > p.x ? p.x : fromX;
                fromY = fromY > p.y ? p.y : fromY;
                toX = toX < p.x ? p.x : toX;
                toY = toY < p.y ? p.y : toY;
            }
            final int w = toX - fromX + 1;
            final int h = toY - fromY + 1;
            final BufferedImage img = new BufferedImage(w * IMG_SIZE, h * IMG_SIZE, BufferedImage.TYPE_INT_ARGB);

            for (final Point p : piecesMap.keySet()) {
                final int x = p.x - fromX;
                final int y = p.y - fromY;
                Graphics2D g = img.createGraphics();
                g.setColor(new Color(piecesMap.get(p).getValue()));
                g.fillRect(x * IMG_SIZE, y * IMG_SIZE, IMG_SIZE, IMG_SIZE);
            }

            return img;
        }

        public String toImage64() {

            final BufferedImage img = toImage();

            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try {
                BMPEncoder.write(img, bs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "data:image/bmp;base64," + Base64.getEncoder().encodeToString(bs.toByteArray());
        }

        public BufferedImage toImageMerge(final PiecesCluster given,
                                   final Point socketPoint,
                                   final Point confluencePoint,
                                   final Side socketSide) {
            final Point sideShift = socketPoint.getBy(socketSide);
            final Point shift = Point.of(
                    - confluencePoint.x + sideShift.x,
                    - confluencePoint.y + sideShift.y
            );
            int fromX = Integer.MAX_VALUE,
                    toX = Integer.MIN_VALUE,
                    fromY = Integer.MAX_VALUE,
                    toY = Integer.MIN_VALUE;
            for (final Point p : piecesMap.keySet()) {
                fromX = fromX > p.x ? p.x : fromX;
                fromY = fromY > p.y ? p.y : fromY;
                toX = toX < p.x ? p.x : toX;
                toY = toY < p.y ? p.y : toY;
            }
            for (final Map.Entry<Point, Piece> entry : given.piecesMap.entrySet()) {

                final Point shifted = Point.of(
                        entry.getKey().x + shift.x,
                        entry.getKey().y + shift.y
                );
                fromX = fromX > shifted.x ? shifted.x : fromX;
                fromY = fromY > shifted.y ? shifted.y : fromY;
                toX = toX < shifted.x ? shifted.x : toX;
                toY = toY < shifted.y ? shifted.y : toY;
            }

            final int w = toX - fromX + 1;
            final int h = toY - fromY + 1;
            final BufferedImage img = new BufferedImage(w * IMG_SIZE, h * IMG_SIZE, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = img.createGraphics();

            for (final Point p : piecesMap.keySet()) {
                final int x = p.x - fromX;
                final int y = p.y - fromY;
                g.setColor(new Color(0x40000000 | piecesMap.get(p).getValue(), true));
                g.fillRect(x * IMG_SIZE, y * IMG_SIZE, IMG_SIZE, IMG_SIZE);
            }

            for (final Point p : given.piecesMap.keySet()) {
                final Point shifted = Point.of(
                        p.x + shift.x,
                        p.y + shift.y
                );
                final int x = shifted.x - fromX;
                final int y = shifted.y - fromY;
                g.setColor(new Color(0xFF000000 | given.piecesMap.get(p).getValue(), true));
                g.fillRect(x * IMG_SIZE, y * IMG_SIZE, IMG_SIZE, IMG_SIZE);
            }

            return img;
        }

        public String toImageMerge64(final PiecesCluster given,
                                     final Point socketPoint,
                                     final Point confluencePoint,
                                     final Side socketSide) {

            final BufferedImage img = toImageMerge(given, socketPoint, confluencePoint, socketSide);

            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try {
                BMPEncoder.write(img, bs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "data:image/bmp;base64," + Base64.getEncoder().encodeToString(bs.toByteArray());
        }

        public Plane toPlane() {

            final Rectangle box = getBoundingBox();
            final BufferedImage img = new BufferedImage(box.width, box.height, BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < box.height; i++) {
                for (int j = 0; j < box.width; j++) {
                    img.setRGB(j, i, piecesMap.get(Point.of(box.x + j, box.y + i)).getValue());
                }
            }
            return new Plane(box.width, box.height, img);
        }
    }

    static class ConfluenceInfo {

        private final PiecesCluster cluster;
        private final Point point;
        private final Piece piece;

        public ConfluenceInfo(final PiecesCluster cluster,
                              final Point point,
                              final Piece piece) {
            this.cluster = cluster;
            this.point = point;
            this.piece = piece;
        }

        public PiecesCluster getCluster() {
            return cluster;
        }

        public Point getPoint() {
            return point;
        }

        public Piece getPiece() {
            return piece;
        }

        @Override
        public String toString() {
            return "ConfluenceInfo{" +
                    "cluster=" + cluster +
                    ", point=" + point +
                    ", piece=" + piece +
                    '}';
        }
    }
}
