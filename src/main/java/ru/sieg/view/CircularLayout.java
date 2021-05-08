package ru.sieg.view;

import java.awt.*;

public class CircularLayout implements LayoutManager {

    private int centralGap;

    public CircularLayout(final int centralGap) {
        this.centralGap = centralGap;
    }

    @Override
    public void addLayoutComponent(final String name, final Component comp) {
        // do nothing.
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
        // do nothing.
    }

    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        return parent.getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        return parent.getMinimumSize();
    }

    @Override
    public void layoutContainer(final Container parent) {

        final int componentCount = parent.getComponentCount();

        if (componentCount == 0) {
            return;
        }

        // placing central component.
        final Component centralComponent = parent.getComponent(0);

        final int xc = parent.getWidth() / 2;
        final int yc = parent.getHeight() / 2;

        final int cc_width = centralComponent.getPreferredSize().width;
        final int cc_height = centralComponent.getPreferredSize().height;

        centralComponent.setBounds(xc - cc_width / 2,
                yc - cc_height / 2,
                cc_width,
                cc_height);

        if (componentCount <= 1) {
            return;
        }

        // placing other components.
        final double r0_2 = sqrSumD(centralComponent.getHeight(), centralComponent.getWidth());
        final double centralComponentRadius = Math.sqrt(r0_2);

        final int circularSize = componentCount - 1;
        for (int i = 0; i < circularSize; i++) {
            final Component currentComponent = parent.getComponent(i + 1);
            final double ri_2 = sqrSumD(currentComponent.getHeight(), currentComponent.getWidth());
            final double currentComponentRadius = Math.sqrt(ri_2);

            final double target_ri = centralComponentRadius + centralGap + currentComponentRadius;
            final double rad = -0.5D * Math.PI + 2.0D * Math.PI * i / circularSize;
            final int xi = (int) (target_ri * Math.cos(rad));
            final int yi = (int) (target_ri * Math.sin(rad));

            final int ci_width = currentComponent.getPreferredSize().width;
            final int ci_height = currentComponent.getPreferredSize().height;

            currentComponent.setBounds(xc + xi - ci_width / 2, yc + yi - ci_height / 2,
                    ci_width, ci_height);
        }
    }

    private static double sqrSumD(final int x, final int y) {
        return 0.25D * (x * x + y * y);
    }
}
