/*
 * JStickyNotes, Copyright (C) Feb 13, 2009 - Jonatan Rico (jrico) jnrico@gmail.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package jrico.jstickynotes.util;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import jrico.jstickynotes.model.Note;

public class Screen {

    private Screen() {
    }

    public static Point getLocation(Note note) {
        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Point relativeLocation = note.getRelativeLocation();
        int x = Percentage.getValue(relativeLocation.x, screenSize.width);
        int y = Percentage.getValue(relativeLocation.y, screenSize.height);
        return new Point(x, y);
    }

    public static Point getRelativeLocation(Point point) {
        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int relativeX = Percentage.getPercentage(point.x, screenSize.width);
        int relativeY = Percentage.getPercentage(point.y, screenSize.height);
        return new Point(relativeX, relativeY);
    }

    public static void center(Window window) {
        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        final int x = (screenSize.width - window.getWidth()) / 2;
        final int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static boolean locate(Window parent, Window child) {
        boolean centeredOnScreen = false;
        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        // first try top/bottom
        Point location = getLocation(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight(),
            child.getWidth(), child.getHeight(), screenSize.width, screenSize.height);

        if (location == null) {
            // try left/right
            location = getLocation(parent.getY(), parent.getX(), parent.getHeight(), parent.getWidth(),
                child.getHeight(), child.getWidth(), screenSize.height, screenSize.width);

            if (location != null) {
                // invert coordinates
                location.setLocation(location.y, location.x);
            }
        }

        if (location != null) {
            child.setLocation(location);
        } else {
            center(child);
            centeredOnScreen = true;
        }

        return centeredOnScreen;
    }

    private static Point getLocation(int parentX, int parentY, int parentWidth, int parentHeight, int childWidth,
            int childHeight, int screenWidth, int screenHeight) {
        Point location = null;
        int gap;

        if ((gap = parentY - childHeight) >= 0) {
            location = new Point(getSecondaryCoordinate(parentX, parentWidth, childWidth, screenWidth),
                gap >= 10 ? gap - 10 : 0);
        } else if ((gap = screenHeight - (parentY + parentHeight + childHeight)) >= 0) {
            location = new Point(getSecondaryCoordinate(parentX, parentWidth, childWidth, screenWidth), parentY
                    + parentHeight + (gap >= 10 ? 10 : gap));
        }

        return location;
    }

    private static int getSecondaryCoordinate(int parentPosition, int parentLength, int childLength, int screenLength) {
        int position = parentLength > childLength ? parentPosition + (parentLength - childLength) / 2 : parentPosition
                - (childLength - parentLength) / 2;

        if (position < 0) {
            position = 0;
        } else if (position + childLength > screenLength) {
            position -= position + childLength - screenLength;
        }

        return position;
    }
}
