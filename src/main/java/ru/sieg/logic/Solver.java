package ru.sieg.logic;

import net.sf.image4j.codec.bmp.BMPEncoder;
import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;
import ru.sieg.view.MainView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
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

    public SolverCompany getSolverCompany() {
        return solverCompany;
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
        while (!pieceRepository.getPieces().isEmpty()) {
            doPieceApproach(view);
        }
        // log
    }

    public void doPieceApproach(final MainView view) {

        if (clusterToConsider == null || boredState) {
            final Optional<Piece> newPiece = pieceRepository.popRandomPiece();
            if (newPiece.isPresent()) {
                clusterToConsider = new PiecesCluster(newPiece.get());
                piecesClusters.add(clusterToConsider);
                boredState = false;
            } else {
                if (!piecesClusters.isEmpty()) {
                    clusterToConsider = piecesClusters.get(random.nextInt(piecesClusters.size()));
                } else {
                    return;
                }
            }
        } else {
            clusterToConsider = piecesClusters.get(random.nextInt(piecesClusters.size()));
        }

        // do process cluster
        // Strategy:
        // 1) select random element of the cluster, connected with air;
        // 2) find an element in the repo, matching with the profile of the selected element;
        // 3) connect;
        //
        //
        //
        System.out.println(getMaxCluster().map(PiecesCluster::size).orElse(0));
        final Optional<Map.Entry<Point, Piece>> _airedPieceEntry = clusterToConsider.getRandomAired();
        if (!_airedPieceEntry.isPresent()) {
            throw new IllegalStateException("Can't obtain any aired piece");
        }
        final Map.Entry<Point, Piece> airedPieceEntry = _airedPieceEntry.get();

        final Side airedSide = clusterToConsider.getRandomAiredSide(airedPieceEntry);
        final Point socket = airedPieceEntry.getKey().getBy(airedSide);

        final Side sideToFind = airedSide.getComplimentary();
        final Profile searchedProfile = airedPieceEntry.getValue().getProfile(airedSide);

        final Optional<PieceRepository.PiecePlace> matched = pieceRepository.findPiece(sideToFind, searchedProfile);
                //.orElseThrow(() -> new IllegalStateException("Can't find matching profile to " + searchedProfile));
        if (matched.isPresent()) {
            final PieceRepository.PiecePlace piecePlace = matched.get();
            clusterToConsider.piecesMap.put(socket, pieceRepository.pop(piecePlace));
            view.show(clusterToConsider.toImage());
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
            } else {
                // try to obtain from other solvers
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
                            System.out.println("solver.clusterToConsider == null");
                        }
                        if (foundClusterInfo.getCluster() != null && foundClusterInfo.getCluster().equals(solver.clusterToConsider)) {
                            solver.clusterToConsider = null;
                        }

                        final boolean forMe = arbitrageWhoTakesCluster(random);
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

                        obtainedFromSolvers = true;

                        break;
                    }
                }
                if (!obtainedFromSolvers) {
                    //System.out.println(piecesClusters.get(0).toImage64());
                    throw new IllegalStateException("Can't either find a piece in repo, or ask any from other solvers");
                }
            }
        }

        // set to bored - strategy can vary todo
        if (random.nextInt(1000) > 900) {
            boredState = true;
        }

        ownedPieces = recalculateOwnedPieces();
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
        //System.out.println(mine.toImageMerge64(given, socketPoint, confluencePoint, socketSide));
        view.show(mine.toImageMerge(given, socketPoint, confluencePoint, socketSide));
        final Point sideShift = socketPoint.getBy(socketSide);
        final Point shift = Point.of(
                - confluencePoint.x + sideShift.x,
                - confluencePoint.y + sideShift.y
        );

        final List<Map.Entry<Point, Piece>> givenEntries = new ArrayList<>(given.piecesMap.entrySet());
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
        return mine;
    }

    private static boolean arbitrageWhoTakesCluster(final Random random) {
        return random.nextBoolean();
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

    static class PiecesCluster {

        private static final int IMG_SIZE = 2;

        private final Map<Point, Piece> piecesMap;
        private final Random random;

        PiecesCluster(final Piece first) {
            piecesMap = new HashMap<>();
            piecesMap.put(Point.ZERO, first);

            random = new Random(System.currentTimeMillis());
        }

        public int size() {
            return piecesMap.size();
        }

        public Piece getFirst() {
            return piecesMap.get(Point.ZERO);
        }

        boolean isAired(final Point point, final Side side) {
            final Point shifted = point.getBy(side);
            return !piecesMap.containsKey(shifted) && !piecesMap.get(point).isEdge(side);
        }

        Optional<Map.Entry<Point, Piece>> getRandomAired() {
            List<Map.Entry<Point, Piece>> list = piecesMap.entrySet().stream()
                    .filter(entry -> {
                        final Point coord = entry.getKey();
                        return isAired(coord, Side.WEST) ||
                                isAired(coord, Side.EAST) ||
                                isAired(coord, Side.NORTH) ||
                                isAired(coord, Side.SOUTH);
                    })
                    .collect(Collectors.toList());
            Collections.shuffle(list, random);
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
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
                g.setColor(new Color(0xFF00FF00));
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
            g.setColor(new Color(0x40FF0000, true));
            for (final Point p : piecesMap.keySet()) {
                final int x = p.x - fromX;
                final int y = p.y - fromY;
                g.fillRect(x * IMG_SIZE, y * IMG_SIZE, IMG_SIZE, IMG_SIZE);
            }

            g.setColor(new Color(0x4000FF00, true));
            for (final Point p : given.piecesMap.keySet()) {
                final Point shifted = Point.of(
                        p.x + shift.x,
                        p.y + shift.y
                );
                final int x = shifted.x - fromX;
                final int y = shifted.y - fromY;
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
