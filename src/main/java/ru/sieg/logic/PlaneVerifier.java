package ru.sieg.logic;

import ru.sieg.logic.domain.Plane;

import java.awt.*;

public class PlaneVerifier {

    public static boolean matches(final Plane reference, final Plane instance) {
        for (int i = 0; i < reference.getVerticalSize(); i++) {
            for (int j = 0; j < reference.getHorizontalSize(); j++) {

                final Color ref_c = reference.getValue(j, i);
                final Color ins_c = instance.getValue(j, i);

                if (!ref_c.equals(ins_c)) {
                    return false;
                }
            }
        }
        return true;
    }
}
