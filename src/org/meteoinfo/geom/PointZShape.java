/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.geom;

/**
 *
 * @author yaqiang
 */
public class PointZShape extends PointShape {
    // <editor-fold desc="Variables">

    //private PointZ _point = new PointZ();
    //private double z;
    //private double m;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public PointZShape() {
        super();
        this.setShapeType(ShapeTypes.PointZ);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

//    /**
//     * Get point
//     *
//     * @return Point
//     */
//    @Override
//    public PointZ getPoint() {
//        return _point;
//    }
//
//    /**
//     * Set point
//     *
//     * @param point Point
//     */
//    public void setPoint(PointZ point) {
//        _point = point;
//    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get M value
     * @return M value
     */
    public double getM(){
        return ((PointZ)this.getPoint()).M;
    }
    
    /**
     * Get Z value
     * @return Z value
     */
    public double getZ(){
        return ((PointZ)this.getPoint()).Z;
    }
    
    /**
     * Clone
     *
     * @return PolylineZShape object
     */
    @Override
    public Object clone() {
        PointZShape aPS = new PointZShape();
        //aPS = (PointZShape)base.Clone();
        aPS.setPoint(this.getPoint());
        //aPS.Z = Z;
        //aPS.M = M;
        aPS.setValue(getValue());
        aPS.setLegendIndex(this.getLegendIndex());

        return aPS;
    }
    // </editor-fold>
}