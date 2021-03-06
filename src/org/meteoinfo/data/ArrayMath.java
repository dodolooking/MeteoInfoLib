/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.data.analysis.MeteoMath;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.shape.PolygonShape;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;

/**
 *
 * @author wyq
 */
public class ArrayMath {

    public static double fill_value = -9999.0;

    // <editor-fold desc="Data type">
    /**
     * Get data type
     *
     * @param o Object
     * @return Data type
     */
    public static DataType getDataType(Object o) {
        if (o instanceof Integer) {
            return DataType.INT;
        } else if (o instanceof Float) {
            return DataType.FLOAT;
        } else if (o instanceof Double) {
            return DataType.DOUBLE;
        } else {
            return DataType.OBJECT;
        }
    }

    private static DataType commonType(DataType aType, DataType bType) {
        if (aType == bType) {
            return aType;
        }

        short anb = ArrayMath.typeToNBytes(aType);
        short bnb = ArrayMath.typeToNBytes(bType);
        if (anb == bnb) {
            switch (aType) {
                case INT:
                case LONG:
                    return bType;
                case FLOAT:
                case DOUBLE:
                    return aType;
            }
        }

        return (anb > bnb) ? aType : bType;
    }

    /**
     * Return the number of bytes per element for the given typecode.
     *
     * @param dataType Data type
     * @return Bytes number
     */
    public static short typeToNBytes(final DataType dataType) {
        switch (dataType) {
            case BYTE:
                return 1;
            case SHORT:
                return 2;
            case INT:
            case FLOAT:
                return 4;
            case LONG:
            case DOUBLE:
                return 8;
            case OBJECT:
                return 0;
            default:
                System.out.println("internal error in typeToNBytes");
                return -1;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Arithmetic">
    /**
     * Array add
     *
     * @param a Array a
     * @param b Array b
     * @return Added array
     */
    public static Array add(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.addInt(a, b);
            case FLOAT:
                return ArrayMath.addFloat(a, b);
            case DOUBLE:
                return ArrayMath.addDouble(a, b);
        }
        return null;
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Number b
     * @return Added array
     */
    public static Array add(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.addInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.addFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.addDouble(a, b.doubleValue());
        }
        return null;
    }

    private static Array addInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE || b.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) + b.getInt(i));
            }
        }

        return r;
    }

    private static Array addInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) + b);
            }
        }

        return r;
    }

    private static Array addFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) + b.getFloat(i));
            }
        }

        return r;
    }

    private static Array addFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) + b);
            }
        }

        return r;
    }

    private static Array addDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) + b.getDouble(i));
            }
        }

        return r;
    }

    private static Array addDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) + b);
            }
        }

        return r;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array sub(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(a, b);
            case FLOAT:
                return ArrayMath.subFloat(a, b);
            case DOUBLE:
                return ArrayMath.subDouble(a, b);
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.subFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.subDouble(a, b.doubleValue());
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.subInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.subFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.subDouble(b.doubleValue(), a);
        }
        return null;
    }

    private static Array subInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) - b.getInt(i));
        }

        return r;
    }

    private static Array subInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) - b);
        }

        return r;
    }

    private static Array subInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b - a.getInt(i));
        }

        return r;
    }

    private static Array subFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) - b.getFloat(i));
        }

        return r;
    }

    private static Array subFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) - b);
        }

        return r;
    }

    private static Array subFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b - a.getFloat(i));
        }

        return r;
    }

    private static Array subDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) - b.getDouble(i));
        }

        return r;
    }

    private static Array subDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) - b);
        }

        return r;
    }

    private static Array subDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b - a.getDouble(i));
        }

        return r;
    }

    /**
     * Array mutiply
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array mul(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.mulInt(a, b);
            case FLOAT:
                return ArrayMath.mulFloat(a, b);
            case DOUBLE:
                return ArrayMath.mulDouble(a, b);
        }
        return null;
    }

    /**
     * Array multiply
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array mul(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.mulInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.mulFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.mulDouble(a, b.doubleValue());
        }
        return null;
    }

    private static Array mulInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE || b.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) * b.getInt(i));
            }
        }

        return r;
    }

    private static Array mulInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) * b);
            }
        }

        return r;
    }

    private static Array mulFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) * b.getFloat(i));
            }
        }

        return r;
    }

    private static Array mulFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) * b);
            }
        }

        return r;
    }

    private static Array mulDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) * b.getDouble(i));
            }
        }

        return r;
    }

    private static Array mulDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) * b);
            }
        }

        return r;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array div(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(a, b);
            case FLOAT:
                return ArrayMath.divFloat(a, b);
            case DOUBLE:
                return ArrayMath.divDouble(a, b);
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.divFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.divDouble(a, b.doubleValue());
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.divInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.divFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.divDouble(b.doubleValue(), a);
        }
        return null;
    }

    private static Array divInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) / b.getInt(i));
        }

        return r;
    }

    private static Array divInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) / b);
        }

        return r;
    }

    private static Array divInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b / a.getInt(i));
        }

        return r;
    }

    private static Array divFloat(Array a, Array b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) / b.getFloat(i));
        }

        return r;
    }

    private static Array divFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) / b);
        }

        return r;
    }

    private static Array divFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b / a.getFloat(i));
        }

        return r;
    }

    private static Array divDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) / b.getDouble(i));
        }

        return r;
    }

    private static Array divDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) / b);
        }

        return r;
    }

    private static Array divDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b / a.getDouble(i));
        }

        return r;
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array pow(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.powInt(a, b.intValue());
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a, b.doubleValue());
        }
        return null;
    }

    /**
     * Array pow function
     *
     * @param a Number a
     * @param b Array b
     * @return Result array
     */
    public static Array pow(Number a, Array b) {
        DataType bType = ArrayMath.getDataType(a);
        DataType type = ArrayMath.commonType(b.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.powInt(a.intValue(), b);
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a.doubleValue(), b);
        }
        return null;
    }
    
    /**
     * Array pow function
     *
     * @param a Number a
     * @param b Array b
     * @return Result array
     */
    public static Array pow(Array a, Array b) {
        DataType bType = ArrayMath.getDataType(a);
        DataType type = ArrayMath.commonType(b.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.powInt(a, b);
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a, b);
        }
        return null;
    }

    private static Array powInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a.getInt(i), b));
        }

        return r;
    }

    private static Array powInt(int a, Array b) {
        Array r = Array.factory(DataType.INT, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a, b.getInt(i)));
        }

        return r;
    }
    
    private static Array powInt(Array a, Array b) {
        Array r = Array.factory(DataType.INT, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a.getInt(i), b.getInt(i)));
        }

        return r;
    }

    private static Array powDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.pow(a.getDouble(i), b));
        }

        return r;
    }

    private static Array powDouble(double a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setDouble(i, Math.pow(a, b.getDouble(i)));
        }

        return r;
    }
    
    private static Array powDouble(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setDouble(i, Math.pow(a.getDouble(i), b.getDouble(i)));
        }

        return r;
    }

    /**
     * Sqrt function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sqrt(Array a) {
        return ArrayMath.pow(a, 0.5);
    }

    /**
     * Exponent function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array exp(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.exp(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Log function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.log(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Log10 function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log10(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.log10(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Array absolute
     *
     * @param a Array a
     * @return Result array
     */
    public static Array abs(Array a) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.abs(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array equal(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) == b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array equal(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            for (int i = 0; i < a.getSize(); i++) {
                if (Double.isNaN(a.getDouble(i))) {
                    r.setDouble(i, 1);
                } else {
                    r.setDouble(i, 0);
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (a.getDouble(i) == v) {
                    r.setDouble(i, 1);
                } else {
                    r.setDouble(i, 0);
                }
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThan(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThan(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThan(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThan(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.doubleValue()) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array notEqual(Array a, Array b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) != b.getDouble(i)) {
                r.setDouble(i, 1);
            } else {
                r.setDouble(i, 0);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array notEqual(Array a, Number b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            for (int i = 0; i < a.getSize(); i++) {
                if (Double.isNaN(a.getDouble(i))) {
                    r.setDouble(i, 0);
                } else {
                    r.setDouble(i, 1);
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (a.getDouble(i) != v) {
                    r.setDouble(i, 1);
                } else {
                    r.setDouble(i, 0);
                }
            }
        }

        return r;
    }

    /**
     * Bit and & operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array bitAnd(Array a, Number b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) & b.intValue());
        }

        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param dx Spacing between all y elements
     * @return Definite integral as approximated by trapezoidal rule
     */
    public static double trapz(Array y, double dx) {
        int n = (int) y.getSize() - 1;
        double a = 1;
        double b = n * dx + a;
        double r = 0;
        for (int i = 0; i < y.getSize(); i++) {
            r += y.getDouble(i);
            if (i > 0 && i < n) {
                r += y.getDouble(i);
            }
        }
        r = r * ((b - a) / (2 * n));
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param dx Spacing between all y elements
     * @param ranges
     * @return Definite integral as approximated by trapezoidal rule
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double trapz(Array y, double dx, List<Range> ranges) throws InvalidRangeException {
        int n = 1;
        for (Range range : ranges) {
            n = n * range.length();
        }
        n -= 1;
        double a = 1;
        double b = n * dx + a;
        double r = 0;
        double v;
        IndexIterator ii = y.getRangeIterator(ranges);
        int i = 0;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            r += v;
            if (i > 0 && i < n) {
                r += v;
            }
            i += 1;
        }
        r = r * ((b - a) / (2 * n));
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param x Spacing array between all y elements
     * @return Definite integral as approximated by trapezoidal rule
     */
    public static double trapz(Array y, Array x) {
        int n = (int) y.getSize() - 1;
        double r = 0;
        for (int i = 0; i < n; i++) {
            r += (x.getDouble(i + 1) - x.getDouble(i)) * (y.getDouble(i + 1) + y.getDouble(i));
        }
        r = r / 2;
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param x Spacing array between all y elements
     * @param ranges
     * @return Definite integral as approximated by trapezoidal rule
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double trapz(Array y, Array x, List<Range> ranges) throws InvalidRangeException {
        double r = 0;
        double v;
        double v0 = Double.NaN;
        IndexIterator ii = y.getRangeIterator(ranges);
        int i = 0;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (Double.isNaN(v0)) {
                v0 = v;
                v = ii.getDoubleNext();
            }
            r += (x.getDouble(i + 1) - x.getDouble(i)) * (v + v0);
            v0 = v;
            i += 1;
        }
        r = r / 2;
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param a Array a
     * @param dx
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array trapz(Array a, double dx, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = trapz(a, dx, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param a Array a
     * @param x Array x
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array trapz(Array a, Array x, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = trapz(a, x, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Matrix">
    /**
     * Matrix multiplication
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array dot(Array a, Array b) {
        int[] shape = new int[2];
        shape[0] = a.getShape()[0];
        shape[1] = b.getShape()[1];
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        Array r = Array.factory(type, shape);
        Index aIndex = a.getIndex();
        Index bIndex = b.getIndex();
        Index rIndex = r.getIndex();
        int n = a.getShape()[1];
        double v;
        for (int i = 0; i < shape[0]; i++) {
            for (int j = 0; j < shape[1]; j++) {
                v = 0;
                for (int m = 0; m < n; m++) {
                    v = v + a.getDouble(aIndex.set(i, m)) * b.getDouble(bIndex.set(m, j));
                }
                r.setDouble(rIndex.set(i, j), v);
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Circular function">
    /**
     * Sine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sin(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.sin(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Cosine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array cos(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.cos(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Tangent function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array tan(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.tan(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Arc sine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array asin(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.asin(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Arc cosine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array acos(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.acos(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Arc tangen function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array atan(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.atan(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Arc tangen function
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array atan2(Array a, Array b) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.atan2(a.getDouble(i), b.getDouble(i)));
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Section/Flip/Transpos...">
    /**
     * Section array
     *
     * @param a Array a
     * @param origin Origin array
     * @param size Size array
     * @param stride Stride array
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, int[] origin, int[] size, int[] stride) throws InvalidRangeException {
        Array r = a.section(origin, size, stride);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Section array
     *
     * @param a Array a
     * @param ranges Ranges
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, List<Range> ranges) throws InvalidRangeException {
        Array r = a.section(ranges);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Number value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, List<Range> ranges, Number v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        while (iter.hasNext()) {
            iter.setObjectNext(v);
        }
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Array value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, List<Range> ranges, Array v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        int i = 0;
        while (iter.hasNext()) {
            iter.setObjectNext(v.getObject(i));
            i += 1;
        }
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Flip array
     *
     * @param a Array a
     * @param idxs Dimension index list
     * @return Result array
     */
    public static Array flip(Array a, List<Integer> idxs) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i : idxs) {
            r = a.flip(i);
        }
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Flip array
     *
     * @param a Array a
     * @param idx Dimension idex
     * @return Result array
     */
    public static Array flip(Array a, int idx) {
        Array r = a.flip(idx);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Transpose array
     *
     * @param a Array a
     * @param dim1 Dimension index 1
     * @param dim2 Dimension index 2
     * @return Result array
     */
    public static Array transpose(Array a, int dim1, int dim2) {
        Array r = a.transpose(dim1, dim2);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Rotate an array by 90 degrees in counter-clockwise direction.
     *
     * @param a The array
     * @param k Rotate times
     * @return Rotated array
     */
    public static Array rot90(Array a, int k) {
        int[] shape = new int[a.getRank()];
        if (Math.abs(k) % 2 == 1) {
            shape[0] = a.getShape()[1];
            shape[1] = a.getShape()[0];
        } else {
            shape[0] = a.getShape()[0];
            shape[1] = a.getShape()[1];
        }
        if (a.getRank() > 2) {
            for (int i = 2; i < a.getRank(); i++) {
                shape[i] = a.getShape()[i];
            }
        }
        Array r = Array.factory(a.getDataType(), shape);
        Index indexa = a.getIndex();
        Index indexr = r.getIndex();
        int[] countera, counterr;
        switch (k) {
            case 1:
            case -3:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = shape[0] - countera[1] - 1;
                    counterr[1] = countera[0];
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            case 2:
            case -2:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = shape[0] - countera[0] - 1;
                    counterr[1] = shape[1] - countera[1] - 1;
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            case 3:
            case -1:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = countera[1];
                    counterr[1] = shape[1] - countera[0] - 1;
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            default:
                r = null;
        }

        return r;
    }

    /**
     * Join two arrays by a dimension
     *
     * @param a Array a
     * @param b Array b
     * @param dim Dimension for join
     * @return Joined array
     */
    public static Array join(Array a, Array b, int dim) {
        int[] shape = a.getShape();
        int na = shape[dim];
        shape[dim] = shape[dim] + b.getShape()[dim];
        int n = shape[dim];
        Array r = Array.factory(a.getDataType(), shape);
        IndexIterator iter = r.getIndexIterator();
        IndexIterator itera = a.getIndexIterator();
        IndexIterator iterb = b.getIndexIterator();
        int[] current;
        int i = 0;
        while (iter.hasNext()) {
            if (i > 0) {
                current = iter.getCurrentCounter();
                if (current[dim] < na - 1 || current[dim] == n - 1) {
                    iter.setObjectNext(itera.getObjectNext());
                } else {
                    iter.setObjectNext(iterb.getObjectNext());
                }
            } else {
                iter.setObjectNext(itera.getObjectNext());
            }
            i += 1;
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Statistics">
    /**
     * Get minimum value
     *
     * @param a Array a
     * @return Minimum value
     */
    public static double getMinimum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        if (min == 1.7976931348623157E+308D) {
            return Double.NaN;
        } else {
            return min;
        }
    }

    /**
     * Get maximum value
     *
     * @param a Array a
     * @return Maximum value
     */
    public static double getMaximum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        if (max == -1.797693134862316E+307D) {
            return Double.NaN;
        } else {
            return max;
        }
    }

    /**
     * Get minimum value
     *
     * @param a Array a
     * @param missingv Missing value
     * @return Minimum value
     */
    public static double getMinimum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Get maximum value
     *
     * @param a Array a
     * @param missingv Missing value
     * @return Maximum value
     */
    public static double getMaximum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    /**
     * Summarize array
     *
     * @param a Array a
     * @return Summarize value
     */
    public static double sumDouble(Array a) {
        double sum = 0.0D;
        double v;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += v;
            }
        }
        return sum;
    }

    /**
     * Summarize array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Summarize value
     */
    public static double sumDouble(Array a, double missingValue) {
        double sum = 0.0D;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
            }
        }
        return sum;
    }

    /**
     * Average array
     *
     * @param a Array a
     * @return Average value
     */
    public static double aveDouble(Array a) {
        double sum = 0.0D;
        double v;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += v;
                n += 1;
            }
        }
        if (n == 0) {
            return Double.NaN;
        } else {
            return sum / n;
        }
    }

    /**
     * Average array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Average value
     */
    public static double aveDouble(Array a, double missingValue) {
        double sum = 0.0D;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
                n += 1;
            }
        }
        return sum / n;
    }

    /**
     * Compute mean value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array mean(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = mean(a, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute mean value of an array
     *
     * @param a Array a
     * @return Mean value
     */
    public static double mean(Array a) {
        double mean = 0.0, v;
        int n = 0;
        for (int i = 0; i < a.getSize(); i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }
        return mean;
    }

    /**
     * Compute mean value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Mean value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double mean(Array a, List<Range> ranges) throws InvalidRangeException {
        double mean = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }
        return mean;
    }

    /**
     * Compute the arithmetic mean arry from a list of arrays
     *
     * @param alist list of arrays
     * @return Mean array
     */
    public static Array mean(List<Array> alist) {
        Array r = Array.factory(DataType.DOUBLE, alist.get(0).getShape());
        double sum, v;
        int n;
        for (int i = 0; i < r.getSize(); i++) {
            sum = 0.0;
            n = 0;
            for (Array a : alist) {
                v = a.getDouble(i);
                if (!Double.isNaN(v)) {
                    sum += v;
                    n += 1;
                }
            }
            if (n > 0) {
                sum = sum / n;
            } else {
                sum = Double.NaN;
            }
            r.setDouble(i, sum);
        }

        return r;
    }
    
    /**
     * Element-wise maximum of array elements.
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The maximum of x1 and x2, element-wise.
     */
    public static Array maximum(Array x1, Array x2){
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++){
            r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
        }
        
        return r;
    }
    
    /**
     * Element-wise maximum of array elements, ignores NaNs.
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The maximum of x1 and x2, element-wise.
     */
    public static Array fmax(Array x1, Array x2){
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++){
            if (Double.isNaN(x1.getDouble(i)))
                r.setObject(i, x2.getDouble(i));
            else if (Double.isNaN(x2.getDouble(i)))
                r.setObject(i, x1.getDouble(i));
            else
                r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
        }
        
        return r;
    }
    
    /**
     * Element-wise minimum of array elements.
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The minimum of x1 and x2, element-wise.
     */
    public static Array minimum(Array x1, Array x2){
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++){
            r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
        }
        
        return r;
    }
    
    /**
     * Element-wise minimum of array elements, ignores NaNs.
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The minimum of x1 and x2, element-wise.
     */
    public static Array fmin(Array x1, Array x2){
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++){
            if (Double.isNaN(x1.getDouble(i)))
                r.setObject(i, x2.getDouble(i));
            else if (Double.isNaN(x2.getDouble(i)))
                r.setObject(i, x1.getDouble(i));
            else
                r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
        }
        
        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Convert">
    /**
     * Set missing value to NaN
     *
     * @param a Array a
     * @param missingv Missing value
     */
    public static void missingToNaN(Array a, Number missingv) {
        if (!a.getDataType().isNumeric())
            return;
        
        IndexIterator iterA = a.getIndexIterator();
        switch (a.getDataType()) {
            case INT:
            case FLOAT:
                while (iterA.hasNext()) {
                    float val = iterA.getFloatNext();
                    if (val == missingv.floatValue()) {
                        iterA.setFloatCurrent(Float.NaN);
                    }
                }
            default:
                while (iterA.hasNext()) {
                    double val = iterA.getDoubleNext();
                    if (MIMath.doubleEquals(val, missingv.doubleValue())) {
                        iterA.setDoubleCurrent(Double.NaN);
                    }
                }
        }
    }

    /**
     * Set value
     *
     * @param a Array a
     * @param b Array b - 0/1 data
     * @param value Value
     */
    public static void setValue(Array a, Array b, Number value) {
        for (int i = 0; i < a.getSize(); i++) {
            if (b.getInt(i) == 1) {
                a.setObject(i, value);
            }
        }
    }
    
    /**
     * Set value
     *
     * @param a Array a
     * @param b Array b - 0/1 data
     * @param value Value array
     */
    public static void setValue(Array a, Array b, Array value) {
        for (int i = 0; i < a.getSize(); i++) {
            if (b.getInt(i) == 1) {
                a.setObject(i, value.getObject(i));
            }
        }
    }

    /**
     * As number list
     *
     * @param a Array a
     * @return Result number list
     */
    public static List<Number> asList(Array a) {
        IndexIterator iterA = a.getIndexIterator();
        List<Number> r = new ArrayList<>();
        switch (a.getDataType()) {
            case SHORT:
            case INT:
                while (iterA.hasNext()) {
                    r.add(iterA.getIntNext());
                }
            case FLOAT:
                while (iterA.hasNext()) {
                    r.add(iterA.getFloatNext());
                }
            case DOUBLE:
                while (iterA.hasNext()) {
                    r.add(iterA.getDoubleNext());
                }
        }
        return r;
    }

    /**
     * Get wind direction and wind speed from U/V
     *
     * @param u U component
     * @param v V component
     * @return Wind direction and wind speed
     */
    public static Array[] uv2ds(Array u, Array v) {
        Array windSpeed = ArrayMath.sqrt(ArrayMath.add(ArrayMath.mul(u, u), ArrayMath.mul(v, v)));
        Array windDir = Array.factory(windSpeed.getDataType(), windSpeed.getShape());
        double ws, wd, U, V;
        for (int i = 0; i < windSpeed.getSize(); i++) {
            U = u.getDouble(i);
            V = u.getDouble(i);
            if (Double.isNaN(U) || Double.isNaN(V)) {
                windDir.setDouble(i, Double.NaN);
                continue;
            }
            ws = windSpeed.getDouble(i);
            if (ws == 0) {
                wd = 0;
            } else {
                wd = Math.asin(U / ws) * 180 / Math.PI;
                if (U < 0 && V < 0) {
                    wd = 180.0 - wd;
                } else if (U > 0 && V < 0) {
                    wd = 180.0 - wd;
                } else if (U < 0 && V > 0) {
                    wd = 360.0 + wd;
                }
                wd += 180;
                if (wd >= 360) {
                    wd -= 360;
                }
            }
            windDir.setDouble(i, wd);
        }

        return new Array[]{windDir, windSpeed};
    }

    /**
     * Get wind U/V components from wind direction and speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @return Wind U/V components
     */
    public static Array[] ds2uv(Array windDir, Array windSpeed) {
        Array U = Array.factory(DataType.DOUBLE, windDir.getShape());
        Array V = Array.factory(DataType.DOUBLE, windDir.getShape());
        double dir;
        for (int i = 0; i < U.getSize(); i++) {
            if (Double.isNaN(windDir.getDouble(i)) || Double.isNaN(windSpeed.getDouble(i))) {
                U.setDouble(i, Double.NaN);
                V.setDouble(i, Double.NaN);
            }
            dir = windDir.getDouble(i) + 180;
            if (dir > 360) {
                dir = dir - 360;
            }
            dir = dir * Math.PI / 180;
            U.setDouble(i, windSpeed.getDouble(i) * Math.sin(dir));
            V.setDouble(i, windSpeed.getDouble(i) * Math.cos(dir));
        }

        return new Array[]{U, V};
    }

    // </editor-fold>       
    // <editor-fold desc="Location">
    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer Polygon vector layer
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, VectorLayer layer) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param ps Polygon shape
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, PolygonShape ps) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(ps);
        return ArrayMath.inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        if (a.getRank() == 2) {
            int xNum = x.size();
            int yNum = y.size();

            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setInt(i * xNum + j, 1);
                    } else {
                        r.setInt(i * xNum + j, -1);
                    }
                }
            }

            return r;
        } else if (a.getRank() == 1) {
            int n = x.size();
            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < n; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setInt(i, 1);
                } else {
                    r.setInt(i, -1);
                }
            }

            return r;
        }

        return null;
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<Number> x_p, List<Number> y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(a, x, y, shapes);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer VectorLayer
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, VectorLayer layer, Number missingValue) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygon Polygon shape
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, PolygonShape polygon, Number missingValue) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(polygon);
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setObject(i, a.getObject(i));
            } else {
                r.setObject(i, Double.NaN);
            }
        }
        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result arrays removing cells outside polygons
     */
    public static Array[] maskout_Remove(Array a, Array x, Array y, List<PolygonShape> polygons) {
        List<Object> rdata = new ArrayList<>();
        List<Double> rxdata = new ArrayList<>();
        List<Double> rydata = new ArrayList<>();
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                rdata.add(a.getObject(i));
                rxdata.add(x.getDouble(i));
                rydata.add(y.getDouble(i));
            }
        }

        int n = rdata.size();
        int[] shape = new int[1];
        shape[0] = n;
        Array r = Array.factory(a.getDataType(), shape);
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < n; i++) {
            r.setObject(i, rdata.get(i));
            rx.setDouble(i, rxdata.get(i));
            ry.setDouble(i, rydata.get(i));
        }

        return new Array[]{r, rx, ry};
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons, Number missingValue) {
        int xNum = x.size();
        int yNum = y.size();

        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getRank() == 1) {
            for (int i = 0; i < xNum; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setObject(i, a.getObject(i));
                } else {
                    r.setObject(i, missingValue);
                }
            }
        } else if (a.getRank() == 2) {
            int idx;
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    idx = i * xNum + j;
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setObject(idx, a.getObject(idx));
                    } else {
                        r.setObject(idx, missingValue);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @param missingValue Missing value
     * @return Result array
     */
    public static Array maskout(Array a, Array m, Number missingValue) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, missingValue);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
    }
    
    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @return Result array
     */
    public static Array maskout(Array a, Array m) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, Double.NaN);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Regress">
    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
        }
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        return (float) r;
    }
    
    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(Array xData, Array yData) {
        int n = (int)xData.getSize();
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        int nn = 0;
        double x, y;
        for (int i = 0; i < n; i++) {
            x = xData.getDouble(i);
            y = yData.getDouble(i);
            if (Double.isNaN(x) || Double.isNaN(y))
                continue;
            x_sum += x;
            y_sum += y;
            sx_sum += x * x;
            sy_sum += y * y;
            xy_sum += x * y;
            nn += 1;
        }

        double r = (nn * xy_sum - x_sum * y_sum) / (Math.sqrt(nn * sx_sum - x_sum * x_sum) * Math.sqrt(nn * sy_sum - y_sum * y_sum));
        return (float) r;
    }

    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept and slope
     */
    public static double[] leastSquareTrend(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumSquareX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += xData.get(i).doubleValue();
            sumY += yData.get(i).doubleValue();
            sumSquareX += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sumXY += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double a = (sumSquareX * sumY - sumX * sumXY) / (n * sumSquareX - sumX * sumX);
        double b = (n * sumXY - sumX * sumY) / (n * sumSquareX - sumX * sumX);

        return new double[]{a, b};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept, slope and correlation coefficent
     */
    public static double[] lineRegress(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double a = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double b = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);

        return new double[]{a, b, r};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept, slope and correlation coefficent
     */
    public static double[] lineRegress(Array xData, Array yData) {
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        int n = 0;
        List<Double> xi = new ArrayList<>();
        List<Double> yi = new ArrayList<>();
        for (int i = 0; i < xData.getSize(); i++) {
            if (Double.isNaN(xData.getDouble(i))) {
                continue;
            }
            if (Double.isNaN(yData.getDouble(i))) {
                continue;
            }
            xi.add(xData.getDouble(i));
            yi.add(yData.getDouble(i));
            x_sum += xData.getDouble(i);
            y_sum += yData.getDouble(i);
            sx_sum += xData.getDouble(i) * xData.getDouble(i);
            sy_sum += yData.getDouble(i) * yData.getDouble(i);
            xy_sum += xData.getDouble(i) * yData.getDouble(i);
            n += 1;
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double intercept = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double slope = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);
        int df = n - 2;
        double TINY = 1.0e-20;
        double t = r * Math.sqrt(df / ((1.0 - r + TINY) * (1.0 + r + TINY)));

        double xbar = x_sum / n;
        double ybar = y_sum / n;
        double bhat = 0.0;
        double ssqx = 0.0;
        for (int i = 0; i < n; i++) {
            bhat = bhat + (yi.get(i) - ybar) * (xi.get(i) - xbar);
            ssqx = ssqx + (xi.get(i) - xbar) * (xi.get(i) - xbar);
        }
        bhat = bhat / ssqx;
        double ahat = ybar - bhat * xbar;
        double sigmahat2 = 0.0;
        double[] ri = new double[n];
        for (int i = 0; i < n; i++) {
            ri[i] = yi.get(i) - (ahat + bhat * xi.get(i));
            sigmahat2 = sigmahat2 + ri[i] * ri[i];
        }
        sigmahat2 = sigmahat2 / (n * 1.0 - 2.0);
        double seb = Math.sqrt(sigmahat2 / ssqx);
        double sigmahat = Math.sqrt((seb * seb) * ssqx);
        double sea = Math.sqrt(sigmahat * sigmahat * (1 / (n * 1.0) + xbar * xbar / ssqx));
        double b0 = 0;
        double Tb = (bhat - b0) / seb;
        double a0 = 0;
        double Ta = (ahat - a0) / sea;
        //double p = studpval(t, df);
        double p = studpval(Ta, n);

        return new double[]{slope, intercept, r, p, n};
    }

    private static double statcom(double mq, int mi, int mj, double mb) {
        double zz = 1;
        double mz = zz;
        int mk = mi;
        while (mk <= mj) {
            zz = zz * mq * mk / (mk - mb);
            mz = mz + zz;
            mk = mk + 2;
        }
        return mz;
    }

    private static double studpval(double mt, int mn) {
        mt = Math.abs(mt);
        double mw = mt / Math.sqrt(mn);
        double th = Math.atan2(mw, 1);
        if (mn == 1) {
            return 1.0 - th / (Math.PI / 2.0);
        }
        double sth = Math.sin(th);
        double cth = Math.cos(th);
        if (mn % 2 == 1) {
            return 1.0 - (th + sth * cth * statcom(cth * cth, 2, mn - 3, -1)) / (Math.PI / 2.0);
        } else {
            return 1.0 - sth * statcom(cth * cth, 1, mn - 3, -1);
        }
    }

    /**
     * Evaluate a polynomial at specific values. If p is of length N, this
     * function returns the value: p[0]*x**(N-1) + p[1]*x**(N-2) + ... +
     * p[N-2]*x + p[N-1]
     *
     * @param p array_like or poly1d object
     * @param x array_like or poly1d object
     * @return ndarray or poly1d
     */
    public static Array polyVal(List<Number> p, Array x) {
        int n = p.size();
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < x.getSize(); i++) {
            double val = x.getDouble(i);
            double rval = 0.0;
            for (int j = 0; j < n; j++) {
                rval += p.get(j).doubleValue() * Math.pow(val, n - j - 1);
            }
            r.setDouble(i, rval);
        }

        return r;
    }

    // </editor-fold>    
    // <editor-fold desc="Meteo">
    /**
     * Performs a centered difference operation on a grid data along one
     * dimension direction
     *
     * @param data The grid data
     * @param dimIdx Direction dimension index
     * @return Result grid data
     */
    public static Array cdiff(Array data, int dimIdx) {
        Array r = Array.factory(DataType.DOUBLE, data.getShape());
        Index index = data.getIndex();
        Index indexr = r.getIndex();
        int[] shape = data.getShape();
        int[] current, cc;
        double a, b;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            if (current[dimIdx] == 0 || current[dimIdx] == shape[dimIdx] - 1) {
                r.setDouble(indexr, Double.NaN);
            } else {
                cc = Arrays.copyOf(current, current.length);
                cc[dimIdx] = cc[dimIdx] - 1;
                index.set(cc);
                a = data.getDouble(index);
                cc[dimIdx] = cc[dimIdx] + 2;
                index.set(cc);
                b = data.getDouble(index);
                if (Double.isNaN(a) || Double.isNaN(b)) {
                    r.setDouble(indexr, Double.NaN);
                } else {
                    r.setDouble(indexr, a - b);
                }
            }
            indexr.incr();
        }

        return r;
    }

    /**
     * Performs a centered difference operation on a grid data in the x or y
     * direction
     *
     * @param data The grid data
     * @param isX If is x direction
     * @return Result grid data
     */
    public static Array cdiff_bak(Array data, boolean isX) {
        if (data.getRank() == 2) {
            int xnum = data.getShape()[1];
            int ynum = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < ynum; i++) {
                for (int j = 0; j < xnum; j++) {
                    if (i == 0 || i == ynum - 1 || j == 0 || j == xnum - 1) {
                        r.setDouble(i * xnum + j, Double.NaN);
                    } else {
                        double a, b;
                        if (isX) {
                            a = data.getDouble(i * xnum + j + 1);
                            b = data.getDouble(i * xnum + j - 1);
                        } else {
                            a = data.getDouble((i + 1) * xnum + j);
                            b = data.getDouble((i - 1) * xnum + j);
                        }
                        if (Double.isNaN(a) || Double.isNaN(b)) {
                            r.setDouble(i * xnum + j, Double.NaN);
                        } else {
                            r.setDouble(i * xnum + j, a - b);
                        }
                    }
                }
            }

            return r;
        } else if (data.getRank() == 1) {
            int n = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < n; i++) {
                if (i == 0 || i == n - 1) {
                    r.setDouble(i, Double.NaN);
                } else {
                    double a, b;
                    a = data.getDouble(i + 1);
                    b = data.getDouble(i - 1);
                    if (Double.isNaN(a) || Double.isNaN(b)) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a - b);
                    }
                }
            }

            return r;
        } else {
            System.out.println("Data dimension number must be 1 or 2!");
            return null;
        }
    }

    /**
     * Calculates the vertical component of the curl (ie, vorticity)
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Curl
     */
    public static Array hcurl(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.get(current[rank - 1]).doubleValue());
            latData.setDouble(index, yy.get(current[rank - 2]).doubleValue());
            index.incr();
        }

        Array dv = cdiff(vData, rank - 1);
        Array dx = mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array du = cdiff(mul(uData, cos(mul(latData, Math.PI / 180))), rank - 2);
        Array dy = mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = div(sub(div(dv, dx), div(du, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Calculates the horizontal divergence using finite differencing
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Divergence
     */
    public static Array hdivg(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.get(current[rank - 1]).doubleValue());
            latData.setDouble(index, yy.get(current[rank - 2]).doubleValue());
            index.incr();
        }

        Array du = cdiff(uData, rank - 1);
        Array dx = mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array dv = cdiff(mul(vData, cos(mul(latData, Math.PI / 180))), rank - 2);
        Array dy = mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = div(add(div(du, dx), div(dv, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Take magnitude value from U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Magnitude grid data
     */
    public static Array magnitude(Array uData, Array vData) {
        int[] shape = uData.getShape();
        int xNum = shape[1];
        int yNum = shape[0];
        int idx;

        Array r = Array.factory(DataType.DOUBLE, shape);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                idx = i * xNum + j;
                if (Double.isNaN(uData.getDouble(idx)) || Double.isNaN(vData.getDouble(idx))) {
                    r.setDouble(idx, Double.NaN);
                } else {
                    r.setDouble(idx, Math.sqrt(Math.pow(uData.getDouble(idx), 2) + Math.pow(vData.getDouble(idx), 2)));
                }
            }
        }

        return r;
    }

    /**
     * Calculate fahrenheit temperature from celsius temperature
     *
     * @param tc Celsius temperature
     * @return Fahrenheit temperature
     */
    public static Array tc2tf(Array tc) {
        Array r = Array.factory(tc.getDataType(), tc.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, MeteoMath.tc2tf(tc.getDouble(i)));
        }

        return r;
    }

    /**
     * Calculate celsius temperature from fahrenheit temperature
     *
     * @param tf Fahrenheit temperature
     * @return Celsius temperature
     */
    public static Array tf2tc(Array tf) {
        Array r = Array.factory(tf.getDataType(), tf.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, MeteoMath.tf2tc(tf.getDouble(i)));
        }

        return r;
    }

    /**
     * Calculate relative humidity from dewpoint
     *
     * @param tdc Dewpoint temperature
     * @param tc Temperature
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array dewpoint2rh(Array tdc, Array tc) {
        Array r = Array.factory(tdc.getDataType(), tdc.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, MeteoMath.dewpoint2rh(tc.getDouble(i), tdc.getDouble(i)));
        }

        return r;
    }

    /**
     * Calculate relative humidity from specific humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, double press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.qair2rh(qair.getDouble(i), temp.getDouble(i), press);
            r.setDouble(i, rh);
        }

        return r;
    }

    /**
     * Calculate relative humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, Array press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.qair2rh(qair.getDouble(i), temp.getDouble(i), press.getDouble(i));
            r.setDouble(i, rh);
        }

        return r;
    }

    /**
     * Calculate height form pressure
     *
     * @param press Pressure
     * @return Height
     */
    public static Array press2Height(Array press) {
        Array r = Array.factory(DataType.DOUBLE, press.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.press2Height(press.getDouble(i));
            r.setDouble(i, rh);
        }

        return r;
    }
    // </editor-fold>
}
