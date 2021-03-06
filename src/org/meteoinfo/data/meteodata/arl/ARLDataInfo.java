/* Copyright 2012 Yaqiang Wang,
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
package org.meteoinfo.data.meteodata.arl;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Dimension;
import org.meteoinfo.data.meteodata.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.GlobalUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.util.DateUtil;
import org.meteoinfo.projection.proj4j.proj.Projection;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.Reproject;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class ARLDataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    //private FileStream _fs = null;
    //private BinaryWriter _bw = null;    
    /// <summary>
    /// Is Lat/Lon
    /// </summary>
    public Boolean isLatLon;
    /// <summary>
    /// Projection info
    /// </summary>
    //public ProjectionInfo projInfo;
    /// <summary>
    /// Data head
    /// </summary>
    public DataHead dataHead;
//    /// <summary>
//    /// Time list
//    /// </summary>
//    public List<Date> times;
    /// <summary>
    /// Record length
    /// </summary>
    public int recLen;
    private int indexLen;
    /// <summary>
    /// Record number per time
    /// </summary>
    public int recsPerTime;
    /// <summary>
    /// Variable list
    /// </summary>
    public List<List<String>> LevelVarList;
//    /// <summary>
//    /// Variables
//    /// </summary>
//    public List<Variable> Variables = new ArrayList<Variable>();
    /// <summary>
    /// Level number
    /// </summary>
    public int levelNum;
    /// <summary>
    /// Level list
    /// </summary>
    public List<Double> levels;
    ///// <summary>
    ///// Variable-levle list
    ///// </summary>
    //public List<ARLVAR> varLevList;
    /// <summary>
    /// Undefine data
    /// </summary>
    public double missingValue;
    /// <summary>
    /// X array
    /// </summary>
    public double[] X;
    /// <summary>
    /// Y array
    /// </summary>
    public double[] Y;
    /// <summary>
    /// Is global
    /// </summary>
    public boolean isGlobal;
    //private DataOutputStream _bw = null;
    private RandomAccessFile _bw = null;
    private long indexRecPos = 0;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ARLDataInfo() {
        isLatLon = false;
        LevelVarList = new ArrayList<>();
        levelNum = 0;
        levels = new ArrayList<>();
        //varLevList = new List<ARLVAR>();
        missingValue = -9999;
        isGlobal = false;
        //projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
        this.setDataType(MeteoDataType.ARL_Grid);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    
    /**
     * Set X
     * @param value X value
     */
    public void setX(List<Number> value){
        this.X = new double[value.size()];
        for (int i = 0; i < value.size(); i++){
            this.X[i] = value.get(i).doubleValue();
        }
    }
    
    /**
     * Set X
     * @param value X value
     */
    public void setY(List<Number> value){
        this.Y = new double[value.size()];
        for (int i = 0; i < value.size(); i++){
            this.Y[i] = value.get(i).doubleValue();
        }
    }

//    /**
//     * Get variable name list
//     *
//     * @return Variable names
//     */
//    @Override
//    public List<String> getVariableNames() {
//        List<String> vNames = new ArrayList<String>();
//        for (Variable aVar : Variables) {
//            vNames.add(aVar.getName());
//        }
//
//        return vNames;
//    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // <editor-fold desc="Read">
    /**
     * If can open as ARL data
     *
     * @param fileName File name
     * @return Boolean
     */
    public static boolean canOpen(String fileName) {
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            DataLabel dl = readDataLabel(br);
            br.close();
            Date t = dl.getTimeValue();
            return t != null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Read data info
     *
     * @param fileName File path
     */
    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            DataLabel aDL;
            DataHead aDH = new DataHead();
            int i, j, vNum;
            String vName;
            List<String> vList = new ArrayList<>();

            //open file to decode the standard label (50) plus the 
            //fixed portion (108) of the extended header   
            aDL = readDataLabel(br);

            byte[] bytes = new byte[4];
            br.read(bytes);
            aDH.MODEL = new String(bytes).trim();
            bytes = new byte[3];
            br.read(bytes);
            aDH.ICX = Integer.parseInt(new String(bytes).trim());
            bytes = new byte[2];
            br.read(bytes);
            aDH.MN = Short.parseShort(new String(bytes).trim());
            bytes = new byte[7];
            br.read(bytes);
            aDH.POLE_LAT = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.POLE_LON = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.REF_LAT = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.REF_LON = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.SIZE = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.ORIENT = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.TANG_LAT = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.SYNC_XP = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.SYNC_YP = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.SYNC_LAT = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.SYNC_LON = Float.parseFloat(new String(bytes).trim());
            br.read(bytes);
            aDH.DUMMY = Float.parseFloat(new String(bytes).trim());
            bytes = new byte[3];
            br.read(bytes);
            aDH.NX = Integer.parseInt(new String(bytes).trim());
            br.read(bytes);
            aDH.NY = Integer.parseInt(new String(bytes).trim());
            br.read(bytes);
            aDH.NZ = Integer.parseInt(new String(bytes).trim());
            bytes = new byte[2];
            br.read(bytes);
            aDH.K_FLAG = Short.parseShort(new String(bytes).trim());
            bytes = new byte[4];
            br.read(bytes);
            aDH.LENH = Integer.parseInt(new String(bytes).trim());

            int NXY = aDH.NX * aDH.NY;
            int LEN = NXY + 50;
            recLen = LEN;
            int indexRecNum = 1;
            indexLen = recLen;

            if (aDH.LENH > NXY) {
                bytes = new byte[NXY - 108];
                br.read(bytes);
                List<Byte> byteList = new ArrayList<>();
                for (byte b : bytes) {
                    byteList.add(b);
                }

                for (i = 0; i < 100; i++) {
                    aDL = readDataLabel(br);
                    if (!aDL.getVarName().equalsIgnoreCase("INDX")) {
                        break;
                    }

                    bytes = new byte[NXY];
                    br.read(bytes);
                    for (byte b : bytes) {
                        byteList.add(b);
                    }
                }
                indexRecNum = i + 1;
                indexLen += i * recLen;
                bytes = new byte[byteList.size()];
                for (i = 0; i < byteList.size(); i++) {
                    bytes[i] = byteList.get(i);
                }

                byte[] nbytes;
                int idx = 0;
                int n;
                for (i = 0; i < aDH.NZ; i++) {
                    n = 6;
                    nbytes = Arrays.copyOfRange(bytes, idx, idx + n);
                    idx += n;
                    String lstr = new String(nbytes);
                    levels.add(Double.parseDouble(lstr.trim()));
                    n = 2;
                    nbytes = Arrays.copyOfRange(bytes, idx, idx + n);
                    vNum = Integer.parseInt(new String(nbytes).trim());
                    idx += n;
                    for (j = 0; j < vNum; j++) {
                        n = 4;
                        nbytes = Arrays.copyOfRange(bytes, idx, idx + n);
                        idx += n;
                        vName = new String(nbytes).trim();
                        vList.add(vName);
                        idx += 4;
                    }
                    LevelVarList.add(new ArrayList<>(vList));
                    vList.clear();
                }
            } else {
                for (i = 0; i < aDH.NZ; i++) {
                    bytes = new byte[6];
                    br.read(bytes);
                    String lstr = new String(bytes);
                    levels.add(Double.parseDouble(lstr.trim()));
                    bytes = new byte[2];
                    br.read(bytes);
                    vNum = Integer.parseInt(new String(bytes).trim());
                    bytes = new byte[4];
                    for (j = 0; j < vNum; j++) {
                        br.read(bytes);
                        vName = new String(bytes).trim();
                        vList.add(vName);
                        br.read(bytes);
                    }
                    LevelVarList.add(new ArrayList<>(vList));
                    vList.clear();
                }
            }
            levelNum = aDH.NZ;

//            if (!aDL.Variable.equals("INDX")) {
//                //ErrorStr = "WARNING Old format meteo data grid!" + Environment.NewLine + aDL.Variable;
//                return;
//            }
            //Decide projection            
            dataHead = aDH;
            if (aDH.SIZE == 0) {
                isLatLon = true;
                X = new double[aDH.NX];
                Y = new double[aDH.NY];
                for (i = 0; i < aDH.NX; i++) {
                    X[i] = aDH.SYNC_LON + i * aDH.REF_LON;
                }
                if (X[aDH.NX - 1] + aDH.REF_LON - X[0] == 360) {
                    isGlobal = true;
                }
                for (i = 0; i < aDH.NY; i++) {
                    Y[i] = aDH.SYNC_LAT + i * aDH.REF_LAT;
                }
            } else {
                //Identify projection
                isLatLon = false;
                String ProjStr;
                ProjectionInfo theProj;
                if (aDH.POLE_LAT == 90 || aDH.POLE_LAT == -90) {
                    if (aDH.TANG_LAT == 90 || aDH.TANG_LAT == -90) {
                        ProjStr = "+proj=stere"
                                + "+lat_0=" + String.valueOf(aDH.TANG_LAT)
                                + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                    } else if (aDH.TANG_LAT == 0) {
                        ProjStr = "+proj=merc"
                                + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                    } else {
                        ProjStr = "+proj=lcc"
                                + "+lat_0=" + String.valueOf(aDH.TANG_LAT)
                                + "+lat_1=" + String.valueOf(aDH.REF_LAT)
                                + "+lat_2=" + String.valueOf(aDH.REF_LAT)
                                + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                    }
                } else if (aDH.TANG_LAT == 0) {
                    ProjStr = "+proj=tmerc"
                            + "+lat_0=" + String.valueOf(aDH.POLE_LAT)
                            + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                } else {
//                    ProjStr = "+proj=stere"
//                            + "+lat_0=" + String.valueOf(aDH.POLE_LAT)
//                            + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                    ProjStr = "+proj=lcc"
                            + "+lat_0=" + String.valueOf(aDH.TANG_LAT)
                            + "+lat_1=" + String.valueOf(aDH.REF_LAT)
                            + "+lat_2=" + String.valueOf(aDH.REF_LAT)
                            + "+lon_0=" + String.valueOf(aDH.REF_LON + aDH.ORIENT);
                }

                theProj = new ProjectionInfo(ProjStr);
                this.setProjectionInfo(theProj);

                //Set X Y
                X = new double[aDH.NX];
                Y = new double[aDH.NY];
                getProjectedXY(theProj, aDH.SIZE * 1000, aDH.SYNC_XP, aDH.SYNC_YP, aDH.SYNC_LON,
                        aDH.SYNC_LAT, X, Y);
            }

            Dimension xDim = new Dimension(DimensionType.X);
            xDim.setValues(X);
            this.setXDimension(xDim);
            Dimension yDim = new Dimension(DimensionType.Y);
            yDim.setValues(Y);
            this.setYDimension(yDim);

            //Reopen            
            byte[] dataBytes = new byte[NXY];
            Date aTime, oldTime;
            int recNum, timeNum;
            br.seek(0);
            recNum = 0;
            timeNum = 0;
            int year = aDL.getYear();
            if (year < 50) {
                year = 2000 + year;
            } else {
                year = 1900 + year;
            }
            Calendar cal = new GregorianCalendar(year, aDL.getMonth() - 1, aDL.getDay(), aDL.getHour(), 0, 0);
            oldTime = cal.getTime();
            List<Date> times = new ArrayList<>();
            times.add((Date) oldTime.clone());

            do {
                if (br.getFilePointer() >= br.length() - 1) {
                    break;
                }

                //Read label
                aDL = readDataLabel(br);

                //Read Data
                br.read(dataBytes);

                if (!aDL.getVarName().equalsIgnoreCase("INDX")) {
                    cal = new GregorianCalendar(year, aDL.getMonth() - 1, aDL.getDay(), aDL.getHour(), 0, 0);
                    aTime = cal.getTime();
                    if (aTime.getTime() != oldTime.getTime()) {
                        times.add(aTime);
                        oldTime.setTime(aTime.getTime());
                        timeNum += 1;
                    }
                    if (timeNum == 0) {
                        recNum += 1;
                    }
                }

            } while (true);

            br.close();

            List<Double> values = new ArrayList<>();
            for (Date t : times) {
                values.add(DateUtil.toOADate(t));
            }
            Dimension tDim = new Dimension(DimensionType.T);
            tDim.setValues(values);
            this.setTimeDimension(tDim);

            recsPerTime = recNum + indexRecNum;
            Variable aVar;
            vList.clear();
            int varIdx;
            List<Variable> variables = new ArrayList<>();
            for (i = 0; i < LevelVarList.size(); i++) {
                for (j = 0; j < LevelVarList.get(i).size(); j++) {
                    vName = LevelVarList.get(i).get(j);
                    if (!vList.contains(vName)) {
                        vList.add(vName);
                        aVar = new Variable();
                        aVar.setName(vName);
                        aVar.getLevels().add(levels.get(i));
                        aVar.getLevelIdxs().add(i);
                        aVar.getVarInLevelIdxs().add(j);
                        variables.add(aVar);
                    } else {
                        varIdx = vList.indexOf(vName);
                        aVar = variables.get(varIdx);
                        aVar.getLevels().add(levels.get(i));
                        aVar.getLevelIdxs().add(i);
                        aVar.getVarInLevelIdxs().add(j);
                        //variables.set(varIdx, aVar);
                    }
                }
            }

            for (Variable var : variables) {
                var.setDimension(this.getTimeDimension());
                Dimension zDim = new Dimension(DimensionType.Z);
                zDim.setValues(var.getLevels());
                var.setDimension(zDim);
                var.setDimension(this.getYDimension());
                var.setDimension(this.getXDimension());
            }
            this.setTimes(times);
            this.setVariables(variables);
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static DataLabel readDataLabel(RandomAccessFile br) {
        try {
            DataLabel aDL = new DataLabel();
            byte[] bytes = new byte[2];
            br.read(bytes);
            aDL.setYear(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setMonth(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setDay(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setHour(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setForecast(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setLevel(Short.parseShort(new String(bytes).trim()));
            br.read(bytes);
            aDL.setGrid(Short.parseShort(new String(bytes).trim()));
            bytes = new byte[4];
            br.read(bytes);
            aDL.setVarName(new String(bytes).trim());
            br.read(bytes);
            aDL.setExponent(Integer.parseInt(new String(bytes).trim()));
            bytes = new byte[14];
            br.read(bytes);
            aDL.setPrecision(Double.parseDouble(new String(bytes).trim()));
            br.read(bytes);
            aDL.setValue(Double.parseDouble(new String(bytes).trim()));
            return aDL;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void getProjectedXY(ProjectionInfo projInfo, float size,
            float sync_XP, float sync_YP, float sync_Lon, float sync_Lat,
            double[] X, double[] Y) {
        //Get sync X/Y
        ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
        double sync_X, sync_Y;
        double[][] points = new double[1][];
        points[0] = new double[]{sync_Lon, sync_Lat};
        Reproject.reprojectPoints(points, fromProj, projInfo, 0, 1);
        sync_X = points[0][0];
        sync_Y = points[0][1];

        //Get integer sync X/Y            
        int i_XP, i_YP;
        double i_X, i_Y;
        i_XP = (int) sync_XP;
        if (sync_XP == i_XP) {
            i_X = sync_X;
        } else {
            i_X = sync_X - (sync_XP - i_XP) * size;
        }
        i_YP = (int) sync_YP;
        if (sync_YP == i_YP) {
            i_Y = sync_Y;
        } else {
            i_Y = sync_Y - (sync_YP - i_YP) * size;
        }

        //Get left bottom X/Y
        int nx, ny;
        nx = X.length;
        ny = Y.length;
        double xlb, ylb;
        xlb = i_X - (i_XP - 1) * size;
        ylb = i_Y - (i_YP - 1) * size;

        //Get X Y with orient 0
        int i;
        for (i = 0; i < nx; i++) {
            X[i] = xlb + i * size;
        }
        for (i = 0; i < ny; i++) {
            Y[i] = ylb + i * size;
        }
    }
    
    /**
     * Get global attributes
     * @return Global attributes
     */
    @Override
    public List<Attribute> getGlobalAttributes(){
        return new ArrayList<>();
    }

    @Override
    public String generateInfoText() {
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
        dataInfo += System.getProperty("line.separator") + "File Start Time: " + format.format(this.getTimes().get(0));
        dataInfo += System.getProperty("line.separator") + "File End Time: " + format.format(this.getTimes().get(this.getTimes().size() - 1));
        dataInfo += System.getProperty("line.separator") + "Record Length Bytes: " + String.valueOf(recLen);
        dataInfo += System.getProperty("line.separator") + "Meteo Data Model: " + dataHead.MODEL;
        dataInfo += System.getProperty("line.separator") + "Xsize = " + String.valueOf(dataHead.NX)
                + "  Ysize = " + String.valueOf(dataHead.NY) + "  Zsize = " + String.valueOf(dataHead.NZ)
                + "  Tsize = " + String.valueOf(this.getTimes().size());
        dataInfo += System.getProperty("line.separator") + "Record Per Time: " + String.valueOf(recsPerTime);
        dataInfo += System.getProperty("line.separator") + "Number of Surface Variables = " + String.valueOf(LevelVarList.get(0).size());
        for (String v : LevelVarList.get(0)) {
            dataInfo += System.getProperty("line.separator") + "  " + v;
        }
        if (LevelVarList.size() > 1) {
            dataInfo += System.getProperty("line.separator") + "Number of Upper Variables = " + String.valueOf(LevelVarList.get(1).size());
            for (String v : LevelVarList.get(1)) {
                dataInfo += System.getProperty("line.separator") + "  " + v;
            }
        }
        dataInfo += System.getProperty("line.separator") + "Pole pnt lat/lon: "
                + String.valueOf(dataHead.POLE_LAT) + "  " + String.valueOf(dataHead.POLE_LON);
        dataInfo += System.getProperty("line.separator") + "Reference pnt lat/lon: "
                + String.valueOf(dataHead.REF_LAT) + "  " + String.valueOf(dataHead.REF_LON);
        dataInfo += System.getProperty("line.separator") + "Grid Size: " + String.valueOf(dataHead.SIZE);
        dataInfo += System.getProperty("line.separator") + "Orientation: " + String.valueOf(dataHead.ORIENT);
        dataInfo += System.getProperty("line.separator") + "Tan lat/cone: " + String.valueOf(dataHead.TANG_LAT);
        dataInfo += System.getProperty("line.separator") + "Syn pnt x/y: " + String.valueOf(dataHead.SYNC_XP)
                + "  " + String.valueOf(dataHead.SYNC_YP);
        dataInfo += System.getProperty("line.separator") + "Syn pnt lat/lon: " + String.valueOf(dataHead.SYNC_LAT)
                + "  " + String.valueOf(dataHead.SYNC_LON);

        return dataInfo;
    }

    private double[][] unpackARLGridData(byte[] dataBytes, int xNum, int yNum, DataLabel aDL) {
        double[][] gridData = new double[yNum][xNum];
        double SCALE = Math.pow(2.0, (7 - aDL.getExponent()));
        double VOLD = aDL.getValue();
        int INDX = 0;
        int i, j;
        for (j = 0; j < yNum; j++) {
            for (i = 0; i < xNum; i++) {
                gridData[j][i] = ((int) (DataConvert.byte2Int(dataBytes[INDX])) - 127) / SCALE + VOLD;
                INDX += 1;
                VOLD = gridData[j][i];
            }
            VOLD = gridData[j][0];
        }

        return gridData;
    }

    private float[] unpackARLData(byte[] dataBytes, int xNum, int yNum, DataLabel aDL) {
        int n = dataBytes.length;
        float[] data = new float[n];
        float SCALE = (float) Math.pow(2.0, (7 - aDL.getExponent()));
        float VOLD = (float) aDL.getValue();
        float init = VOLD;
        float v;
        int INDX = 0;
        int i, j;
        for (j = 0; j < yNum; j++) {
            for (i = 0; i < xNum; i++) {
                v = ((int) (DataConvert.byte2Int(dataBytes[INDX])) - 127) / SCALE + VOLD;
                data[INDX] = v;
                if (i == 0) {
                    init = v;
                }
                INDX += 1;
                VOLD = v;
            }
            VOLD = init;
        }

        return data;
    }
    
    /**
     * Read array data of a variable
     * 
     * @param varName Variable name
     * @return Array data
     */
    @Override
    public Array read(String varName){
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++){
            origin[i] = 0;
            size[i] = var.getDimLength(i);
            stride[i] = 1;
        }
        
        Array r = read(varName, origin, size, stride);
        
        return r;
    }

    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @param origin The origin array
     * @param size The size array
     * @param stride The stride array
     * @return Array data
     */
    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            Variable var = this.getVariable(varName);
            Section section = new Section(origin, size, stride);
            Array dataArray = Array.factory(DataType.FLOAT, section.getShape());
            int rangeIdx = 0;
            Range timeRange = section.getRank() > 2 ? section
                    .getRange(rangeIdx++)
                    : new Range(0, 0);

            Range levRange = var.getLevelNum() > 0 ? section
                    .getRange(rangeIdx++)
                    : new Range(0, 0);

            Range yRange = section.getRange(rangeIdx++);
            Range xRange = section.getRange(rangeIdx);

            IndexIterator ii = dataArray.getIndexIterator();

            for (int timeIdx = timeRange.first(); timeIdx <= timeRange.last();
                    timeIdx += timeRange.stride()) {
                int levelIdx = levRange.first();

                for (; levelIdx <= levRange.last();
                        levelIdx += levRange.stride()) {
                    readXY(varName, timeIdx, levelIdx, yRange, xRange, ii);
                }
            }

            return dataArray;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void readXY(String varName, int timeIdx, int levelIdx, Range yRange, Range xRange, IndexIterator ii) {
        try {
            int varIdx = this.getVariableNames().indexOf(varName);
            int xNum, yNum;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }
            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());
            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            for (int i = 0; i < levelIdx; i++) {
                br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
            }
            br.seek(br.getFilePointer() + varIdx * recLen);
            //Read label
            aDL = ARLDataInfo.readDataLabel(br);
            //Read Data
            dataBytes = new byte[recLen - 50];
            br.read(dataBytes);
            br.close();
            float[] data = unpackARLData(dataBytes, xNum, yNum, aDL);
            for (int y = yRange.first(); y <= yRange.last();
                    y += yRange.stride()) {
                for (int x = xRange.first(); x <= xRange.last();
                        x += xRange.stride()) {
                    int index = y * xNum + x;
                    ii.setFloatNext(data[index]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get grid data
     *
     * @param varName Variable name
     * @return Grid data
     */
    @Override
    public GridArray getGridArray(String varName) {
        return null;    
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        try {
            int xNum, yNum;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            for (int i = 0; i < levelIdx; i++) {
                br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
            }
            br.seek(br.getFilePointer() + varIdx * recLen);

            //Read label
            aDL = ARLDataInfo.readDataLabel(br);

            //Read Data
            dataBytes = new byte[recLen - 50];
            br.read(dataBytes);

            br.close();

            theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);

            GridData gridData = new GridData();
            gridData.data = theData;
            gridData.missingValue = missingValue;
            gridData.xArray = X;
            gridData.yArray = Y;

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        try {
            int xNum, yNum, tNum, t;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            tNum = this.getTimeNum();
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            double[][] newGridData = new double[tNum][yNum];

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            for (t = 0; t < tNum; t++) {
                br.seek(t * recsPerTime * recLen);
                br.seek(br.getFilePointer() + indexLen);
                for (int i = 0; i < levelIdx; i++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
                }
                br.seek(br.getFilePointer() + varIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);
                for (int i = 0; i < yNum; i++) {
                    newGridData[t][i] = theData[i][lonIdx];
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = newGridData;
            gridData.missingValue = missingValue;
            gridData.xArray = Y;
            gridData.yArray = new double[tNum];
            for (int i = 0; i < tNum; i++) {
                gridData.yArray[i] = DateUtil.toOADate(this.getTimes().get(i));
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        try {
            int xNum, yNum, tNum, t;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            tNum = this.getTimeNum();
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            double[][] newGridData = new double[tNum][xNum];

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            for (t = 0; t < tNum; t++) {
                br.seek(t * recsPerTime * recLen);
                br.seek(br.getFilePointer() + indexLen);
                for (int i = 0; i < levelIdx; i++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
                }
                br.seek(br.getFilePointer() + varIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);
                for (int j = 0; j < xNum; j++) {
                    newGridData[t][j] = theData[latIdx][j];
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = newGridData;
            gridData.missingValue = missingValue;
            gridData.xArray = X;
            gridData.yArray = new double[tNum];
            for (int i = 0; i < tNum; i++) {
                gridData.yArray[i] = DateUtil.toOADate(this.getTimes().get(i));
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        try {
            int xNum, yNum, lNum, nvarIdx, levIdx;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            lNum = this.getVariables().get(varIdx).getLevelNum();
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            double[][] newGridData = new double[lNum][yNum];
            long aLevPosition;

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            aLevPosition = br.getFilePointer();
            //levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(0);
            for (int i = 0; i < lNum; i++) {
                nvarIdx = this.getVariables().get(varIdx).getVarInLevelIdxs().get(i);
                levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(i);
                br.seek(aLevPosition);
                for (int j = 0; j < levIdx; j++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(j).size() * recLen);
                }
                br.seek(br.getFilePointer() + nvarIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);
                for (int j = 0; j < yNum; j++) {
                    newGridData[i][j] = theData[j][lonIdx];
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = newGridData;
            gridData.missingValue = missingValue;
            gridData.xArray = Y;
            gridData.yArray = new double[lNum];
            for (int i = 0; i < lNum; i++) {
                gridData.yArray[i] = this.getVariables().get(varIdx).getLevels().get(i);
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        try {
            int xNum, yNum, lNum, nvarIdx, levIdx;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            lNum = this.getVariables().get(varIdx).getLevelNum();
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            double[][] newGridData = new double[lNum][xNum];
            long aLevPosition;

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            aLevPosition = br.getFilePointer();
            //levIdx = Variables[cvarIdx].LevelIdxs[0];
            for (int i = 0; i < lNum; i++) {
                nvarIdx = this.getVariables().get(varIdx).getVarInLevelIdxs().get(i);
                levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(i);
                br.seek(aLevPosition);
                for (int j = 0; j < levIdx; j++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(j).size() * recLen);
                }
                br.seek(br.getFilePointer() + nvarIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);
                for (int j = 0; j < xNum; j++) {
                    newGridData[i][j] = theData[latIdx][j];
                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = newGridData;
            gridData.missingValue = missingValue;
            gridData.xArray = X;
            gridData.yArray = new double[lNum];
            for (int i = 0; i < lNum; i++) {
                gridData.yArray[i] = this.getVariables().get(varIdx).getLevels().get(i);
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        try {
            int xNum, yNum, lNum, nvarIdx, levIdx, t, tNum;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            lNum = this.getVariables().get(varIdx).getLevelNum();
            tNum = this.getTimeNum();
            double[][] theData;
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            double[][] newGridData = new double[lNum][tNum];
            long aLevPosition;

            for (t = 0; t < tNum; t++) {
                br.seek(t * recsPerTime * recLen);
                br.seek(br.getFilePointer() + indexLen);
                aLevPosition = br.getFilePointer();
                //levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(0);
                for (int i = 0; i < lNum; i++) {
                    nvarIdx = this.getVariables().get(varIdx).getVarInLevelIdxs().get(i);
                    levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(i);
                    br.seek(aLevPosition);
                    for (int j = 0; j < levIdx; j++) {
                        br.seek(br.getFilePointer() + LevelVarList.get(j).size() * recLen);
                    }
                    br.seek(br.getFilePointer() + nvarIdx * recLen);

                    //Read label
                    aDL = ARLDataInfo.readDataLabel(br);

                    //Read Data
                    dataBytes = new byte[recLen - 50];
                    br.read(dataBytes);
                    theData = unpackARLGridData(dataBytes, xNum, yNum, aDL);

                    newGridData[i][t] = theData[latIdx][lonIdx];

                }
            }

            br.close();

            GridData gridData = new GridData();
            gridData.data = newGridData;
            gridData.missingValue = missingValue;
            gridData.xArray = new double[tNum];
            for (int i = 0; i < tNum; i++) {
                gridData.xArray[i] = DateUtil.toOADate(this.getTimes().get(i));
            }
            gridData.yArray = new double[lNum];
            for (int i = 0; i < lNum; i++) {
                gridData.yArray[i] = this.getVariables().get(varIdx).getLevels().get(i);
            }

            return gridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            int xNum, yNum, t;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            double[][] gridData;
            double aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = missingValue;
            aGridData.xArray = new double[this.getTimeNum()];
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][this.getTimeNum()];

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            for (t = 0; t < this.getTimeNum(); t++) {
                br.seek(t * recsPerTime * recLen);
                br.seek(br.getFilePointer() + indexLen);
                for (int i = 0; i < levelIdx; i++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
                }
                br.seek(br.getFilePointer() + varIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                gridData = unpackARLGridData(dataBytes, xNum, yNum, aDL);

                aValue = gridData[latIdx][lonIdx];
                aGridData.xArray[t] = DateUtil.toOADate(this.getTimes().get(t));
                aGridData.data[0][t] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            int xNum, yNum, nvarIdx, levIdx, lNum;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            lNum = this.getVariables().get(varIdx).getLevelNum();
            double[][] gridData;
            double aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = missingValue;
            aGridData.xArray = new double[lNum];
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][lNum];

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            long aLevPosition = br.getFilePointer();
            //levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(0);
            for (int i = 0; i < lNum; i++) {
                nvarIdx = this.getVariables().get(varIdx).getVarInLevelIdxs().get(i);
                levIdx = this.getVariables().get(varIdx).getLevelIdxs().get(i);
                br.seek(aLevPosition);
                for (int j = 0; j < levIdx; j++) {
                    br.seek(br.getFilePointer() + LevelVarList.get(j).size() * recLen);
                }
                br.seek(br.getFilePointer() + nvarIdx * recLen);

                //Read label
                aDL = ARLDataInfo.readDataLabel(br);

                //Read Data
                dataBytes = new byte[recLen - 50];
                br.read(dataBytes);
                gridData = unpackARLGridData(dataBytes, xNum, yNum, aDL);
                aValue = gridData[latIdx][lonIdx];
                aGridData.xArray[i] = levels.get(levIdx);
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            int xNum, yNum, i;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            double[][] gridData;
            double aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = missingValue;
            aGridData.xArray = X;
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][X.length];

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            for (i = 0; i < levelIdx; i++) {
                br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
            }
            br.seek(br.getFilePointer() + varIdx * recLen);

            //Read label
            aDL = ARLDataInfo.readDataLabel(br);

            //Read Data
            dataBytes = new byte[recLen - 50];
            br.read(dataBytes);
            gridData = unpackARLGridData(dataBytes, xNum, yNum, aDL);

            for (i = 0; i < xNum; i++) {
                aValue = gridData[latIdx][i];
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            byte[] dataBytes;
            DataLabel aDL;
            int xNum, yNum, i;
            xNum = dataHead.NX;
            yNum = dataHead.NY;
            double[][] gridData;
            double aValue;

            GridData aGridData = new GridData();
            aGridData.missingValue = missingValue;
            aGridData.xArray = Y;
            aGridData.yArray = new double[1];
            aGridData.yArray[0] = 0;
            aGridData.data = new double[1][Y.length];

            //Update level and variable index
            Variable aVar = this.getVariables().get(varIdx);
            if (aVar.getLevelNum() > 1) {
                levelIdx += 1;
            }

            varIdx = LevelVarList.get(levelIdx).indexOf(aVar.getName());

            br.seek(timeIdx * recsPerTime * recLen);
            br.seek(br.getFilePointer() + indexLen);
            for (i = 0; i < levelIdx; i++) {
                br.seek(br.getFilePointer() + LevelVarList.get(i).size() * recLen);
            }
            br.seek(br.getFilePointer() + varIdx * recLen);

            //Read label
            aDL = ARLDataInfo.readDataLabel(br);

            //Read Data
            dataBytes = new byte[recLen - 50];
            br.read(dataBytes);
            gridData = unpackARLGridData(dataBytes, xNum, yNum, aDL);

            for (i = 0; i < yNum; i++) {
                aValue = gridData[i][lonIdx];
                aGridData.data[0][i] = aValue;
            }

            br.close();

            return aGridData;
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Write">

    /**
     * Create ARL binary data file
     *
     * @param fileName File name
     */
    public void createDataFile(String fileName) {
        try {
            //_bw = new DataOutputStream(new FileOutputStream(new File(fileName)));
            _bw = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Close the data file created by previos step
     */
    public void closeDataFile() {
        try {
            _bw.close();
        } catch (IOException ex) {
            Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get data header of index record
     *
     * @param projInfo Projection info
     * @param model Data source
     * @param kFlag Level flag
     * @param icx Forecasting hour
     * @param mn Minutes
     * @return The data header
     */
    public DataHead getDataHead(ProjectionInfo projInfo, String model, int kFlag, int icx, short mn) {
        this.setIndexRecPos();
        
        int i;
        DataHead aDH = new DataHead();
        aDH.MODEL = model;
        aDH.ICX = icx;
        aDH.MN = mn;
        aDH.K_FLAG = (short) kFlag;
        aDH.LENH = 108;
        for (i = 0; i < levels.size(); i++) {
            aDH.LENH += this.LevelVarList.get(i).size() * 8 + 8;
        }

        if (projInfo.getProjectionName() == ProjectionNames.LongLat) {
            aDH.POLE_LAT = 90;
            aDH.POLE_LON = 0;
            aDH.REF_LAT = (float) (Y[1] - Y[0]);
            aDH.REF_LON = (float) (X[1] - X[0]);
            aDH.SIZE = 0;
            aDH.ORIENT = 0;
            aDH.TANG_LAT = 0;
            aDH.SYNC_XP = 1;
            aDH.SYNC_YP = 1;
            aDH.SYNC_LAT = (float) Y[0];
            aDH.SYNC_LON = (float) X[0];
            aDH.DUMMY = 0;
            aDH.NX = X.length;
            aDH.NY = Y.length;
            aDH.NZ = levels.size();
        } else {
            float sync_x, sync_y;
            sync_x = 0.5f * (X.length + 1);
            sync_y = 0.5f * (Y.length + 1);
            double sync_lon, sync_lat;
            if (Double.isNaN(projInfo.getCenterLon())) {
                double x = MIMath.getValue(X, sync_x);
                double y = MIMath.getValue(Y, sync_y);
                double[][] points = new double[1][];
                points[0] = new double[]{x, y};
                ProjectionInfo toProj = KnownCoordinateSystems.geographic.world.WGS1984;
                Reproject.reprojectPoints(points, projInfo, toProj, 0, 1);
                sync_lon = points[0][0];
                sync_lat = points[0][1];
            } else {
                sync_lon = projInfo.getCenterLon();
                sync_lat = projInfo.getCenterLat();
            }
            Projection aProj = projInfo.getCoordinateReferenceSystem().getProjection();
            double tanLat = this.eqvlat(aProj.getProjectionLatitude1Degrees(),
                    aProj.getProjectionLatitude2Degrees());
            tanLat = Double.parseDouble(String.format("%.2f", tanLat));
            switch (projInfo.getProjectionName()) {
                case Lambert_Conformal_Conic:
                    aDH.POLE_LAT = (float) tanLat;
                    aDH.POLE_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.REF_LAT = (float) tanLat;
                    aDH.REF_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.SIZE = (float) (X[1] - X[0]) / 1000;
                    aDH.ORIENT = 0;
                    aDH.TANG_LAT = (float) tanLat;
                    aDH.SYNC_XP = sync_x;
                    aDH.SYNC_YP = sync_y;
                    aDH.SYNC_LAT = (float) sync_lat;
                    aDH.SYNC_LON = (float) sync_lon;
                    aDH.DUMMY = 0;
                    aDH.NX = X.length;
                    aDH.NY = Y.length;
                    aDH.NZ = levels.size();
                    break;
                case Mercator:
                    aDH.POLE_LAT = 0;
                    aDH.POLE_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.REF_LAT = (float) tanLat;
                    aDH.REF_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.SIZE = (float) (X[1] - X[0]) / 1000;
                    aDH.ORIENT = 0;
                    aDH.TANG_LAT = (float) tanLat;
                    aDH.SYNC_XP = sync_x;
                    aDH.SYNC_YP = sync_y;
                    aDH.SYNC_LAT = (float) sync_lat;
                    aDH.SYNC_LON = (float) sync_lon;
                    aDH.DUMMY = 0;
                    aDH.NX = X.length;
                    aDH.NY = Y.length;
                    aDH.NZ = levels.size();
                    break;
                case North_Polar_Stereographic_Azimuthal:
                    aDH.POLE_LAT = 90;
                    aDH.POLE_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.REF_LAT = (float) tanLat;
                    aDH.REF_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.SIZE = (float) (X[1] - X[0]) / 1000;
                    aDH.ORIENT = 0;
                    aDH.TANG_LAT = (float) aProj.getProjectionLatitude2Degrees();
                    aDH.SYNC_XP = sync_x;
                    aDH.SYNC_YP = sync_y;
                    aDH.SYNC_LAT = (float) sync_lat;
                    aDH.SYNC_LON = (float) sync_lon;
                    aDH.DUMMY = 0;
                    aDH.NX = X.length;
                    aDH.NY = Y.length;
                    aDH.NZ = levels.size();
                    break;
                case South_Polar_Stereographic_Azimuthal:
                    aDH.POLE_LAT = -90;
                    aDH.POLE_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.REF_LAT = (float) tanLat;
                    aDH.REF_LON = (float) aProj.getProjectionLongitudeDegrees();
                    aDH.SIZE = (float) (X[1] - X[0]) / 1000;
                    aDH.ORIENT = 0;
                    aDH.TANG_LAT = (float) aProj.getProjectionLatitude2Degrees();
                    aDH.SYNC_XP = 1;
                    aDH.SYNC_YP = 1;
                    aDH.SYNC_LAT = (float) sync_lat;
                    aDH.SYNC_LON = (float) sync_lon;
                    aDH.DUMMY = 0;
                    aDH.NX = X.length;
                    aDH.NY = Y.length;
                    aDH.NZ = levels.size();
                    break;
            }
        }

        return aDH;
    }

    private double eqvlat(double lat1, double lat2) {
        double RADPDEG = Math.PI / 180.0;
        double DEGPRAD = 180.0 / Math.PI;
        double slat1, slat2, al1, al2;
        slat1 = Math.sin(RADPDEG * lat1);
        slat2 = Math.sin(RADPDEG * lat2);
        /* reorder, slat1 larger */
        if (slat1 < slat2) {
            double temp = slat1;
            slat1 = slat2;
            slat2 = temp;
        }
        /*  Take care of special cases first */
        if (slat1 == slat2) {
            return Math.asin(slat1) * DEGPRAD;
        }
        if (slat1 == -slat2) {
            return 0.0;
        }
        if (slat1 >= 1.0) {
            return 90.0;
        }
        if (slat2 <= -1.0) {
            return -90.0;
        }
        /**
         * *****************************************************
         */
        double FSM = 1.0e-3;
        /* Compute al1 = log((1. - slat1)/(1. - slat2))/(slat1 - slat2) */
        {
            double tau = (slat1 - slat2) / (2.0 - slat1 - slat2);
            if (tau > FSM) {
                al1 = Math.log((1.0 - slat1) / (1.0 - slat2)) / (slat1 - slat2);
            } else {
                tau *= tau;
                al1 = -2.0 / (2.0 - slat1 - slat2)
                        * (1.0 + tau
                        * (1.0 / 3.0 + tau
                        * (1.0 / 5.0 + tau
                        * (1.0 / 7.0 + tau))));
            }
        }
        /* Compute al2 = log((1. + slat1)/(1. + slat2))/(slat1 - slat2) */
        {
            double tau = (slat1 - slat2) / (2.0 + slat1 + slat2);
            if (tau > FSM) {
                al2 = Math.log((1.0 + slat1) / (1.0 + slat2)) / (slat1 - slat2);
            } else {
                tau *= tau;
                al2 = 2.0 / (2.0 + slat1 + slat2)
                        * (1.0 + tau
                        * (1.0 / 3.0 + tau
                        * (1.0 / 5.0 + tau
                        * (1.0 / 7.0 + tau))));
            }
        }
        double eqvlat = Math.asin((al1 + al2) / (al1 - al2)) * DEGPRAD;
        return eqvlat;
    }

    private double eqvlat_bak(double lat1, double lat2) {
        //  WRITTEN ON 3/31/94 BY Dr. Albion Taylor  NOAA / OAR / ARL
        double RADPDG = Math.PI / 180.;
        double SINL1 = Math.sin(lat1 * RADPDG);
        double SINL2 = Math.sin(lat2 * RADPDG);
        double AL1, AL2;
        if (Math.abs(SINL1 - SINL2) > .001) {
            AL1 = Math.log((1. - SINL1) / (1. - SINL2));
            AL2 = Math.log((1. + SINL1) / (1. + SINL2));
        } else {
            //  CASE LAT1 NEAR OR EQUAL TO LAT2
            double TAU = -(SINL1 - SINL2) / (2. - SINL1 - SINL2);
            TAU = TAU * TAU;
            AL1 = 2. / (2. - SINL1 - SINL2) * (1. + TAU
                    * (1. / 3. + TAU
                    * (1. / 5. + TAU
                    * (1. / 7.))));
            TAU = (SINL1 - SINL2) / (2. + SINL1 + SINL2);
            TAU = TAU * TAU;
            AL2 = -2. / (2. + SINL1 + SINL2) * (1. + TAU
                    * (1. / 3. + TAU
                    * (1. / 5. + TAU
                    * (1. / 7.))));
        }
        double EQVLAT = Math.asin((AL1 + AL2) / (AL1 - AL2)) / RADPDG;
        return EQVLAT;
    }

    private String padNumStr(String str, int n) {
        String nstr = str;
        if (nstr.indexOf('.') < 0) {
            nstr = nstr + ".";
        }
        if (nstr.length() > 6) {
            nstr = nstr.substring(0, 6);
        }
        return GlobalUtil.padRight(nstr, n, '0');
    }
    
    /**
     * Set index record position
     */
    public void setIndexRecPos(){
        if (_bw != null) {
            try {
                this.indexRecPos = this._bw.getFilePointer();
            } catch (IOException ex) {
                Logger.getLogger(ARLDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Write index record
     *
     * @param time The time
     * @param aDH The data header
     * @param ksums Checksum list
     * @throws java.io.IOException
     */
    public void writeIndexRecord(Date time, DataHead aDH, List<List<Integer>> ksums) throws IOException {
        _bw.seek(this.indexRecPos);
        //write the standard label (50) plus the 
        //fixed portion (108) of the extended header   
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
        String dateStr = format.format(time);
        _bw.writeBytes(dateStr);
        _bw.writeBytes("00");
        _bw.writeBytes("00");
        _bw.writeBytes("11");
        _bw.writeBytes("INDX");
        _bw.writeBytes("   0");
        _bw.writeBytes(" 0.0000000E+00");
        _bw.writeBytes(" 0.0000000E+00");

        _bw.writeBytes(GlobalUtil.padRight(aDH.MODEL, 4, '1'));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.ICX), 3, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.MN), 2, ' '));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.POLE_LAT), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.POLE_LON), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.REF_LAT), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.REF_LON), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.SIZE), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.ORIENT), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.TANG_LAT), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.SYNC_XP), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.SYNC_YP), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.SYNC_LAT), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.SYNC_LON), 7));
        _bw.writeBytes(padNumStr(String.valueOf(aDH.DUMMY), 7));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.NX), 3, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.NY), 3, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.NZ), 3, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.K_FLAG), 2, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDH.LENH), 4, ' '));
        String levStr, ksumStr;
        for (int i = 0; i < aDH.NZ; i++) {
            levStr = padNumStr(String.valueOf(levels.get(i)), 6);
            _bw.writeBytes(levStr);
            int vNum = LevelVarList.get(i).size();
            _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(vNum), 2, ' '));
            for (int j = 0; j < vNum; j++) {
                _bw.writeBytes(GlobalUtil.padRight(LevelVarList.get(i).get(j), 4, '1'));
                if (ksums == null){
                    _bw.writeBytes("226");
                } else {
                    ksumStr = GlobalUtil.padLeft(ksums.get(i).get(j).toString(), 3, ' ');
                    _bw.writeBytes(ksumStr);
                }
                _bw.writeBytes(" ");
            }
        }
        _bw.write(new byte[aDH.NY * aDH.NX - aDH.LENH]);
        _bw.seek(_bw.length());
    }

    /**
     * Write grid data
     *
     * @param aDL The data label
     * @param gridData The grid data
     * @throws java.io.IOException
     */
    public void writeGridData(DataLabel aDL, GridData gridData) throws IOException {
        byte[] dataBytes = packARLGridData(gridData, aDL);

        //write data label
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
        String dateStr = format.format(aDL.getTime());
        _bw.writeBytes(dateStr);
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getForecast()), 2, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getLevel()), 2, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getGrid()), 2, ' '));
        _bw.writeBytes(GlobalUtil.padRight(aDL.getVarName(), 4, '1'));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getExponent()), 4, ' '));
        DecimalFormat dformat = new DecimalFormat("0.0000000E00");
        String preStr = dformat.format(aDL.getPrecision());
        if (!preStr.contains("E-")) {
            preStr = preStr.replace("E", "E+");
        }
        _bw.writeBytes(GlobalUtil.padLeft(preStr, 14, ' '));
        preStr = dformat.format(aDL.getValue());
        if (!preStr.contains("E-")) {
            preStr = preStr.replace("E", "E+");
        }
        _bw.writeBytes(GlobalUtil.padLeft(preStr, 14, ' '));

        //Write data
        _bw.write(dataBytes);
    }
    
    /**
     * Write grid data
     *
     * @param aDL The data label
     * @param a The data array
     * @return Check sum
     * @throws java.io.IOException
     */
    public int writeGridData(DataLabel aDL, Array a) throws IOException {
        Object[] r = packARLGridData(a, aDL);
        byte[] dataBytes = (byte[])r[0];
        int ksum = (int)r[1];       

        //write data label
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
        String dateStr = format.format(aDL.getTime());
        _bw.writeBytes(dateStr);
        String fcst = GlobalUtil.padLeft(String.valueOf(aDL.getForecast()), 2, ' ');
        if (fcst.length() > 2)
            fcst = fcst.substring(fcst.length() - 2);
        _bw.writeBytes(fcst);
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getLevel()), 2, ' '));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getGrid()), 2, ' '));
        _bw.writeBytes(GlobalUtil.padRight(aDL.getVarName(), 4, '1'));
        _bw.writeBytes(GlobalUtil.padLeft(String.valueOf(aDL.getExponent()), 4, ' '));
        DecimalFormat dformat = new DecimalFormat("0.0000000E00");
        String preStr = dformat.format(aDL.getPrecision());
        if (!preStr.contains("E-")) {
            preStr = preStr.replace("E", "E+");
        }
        _bw.writeBytes(GlobalUtil.padLeft(preStr, 14, ' '));
        preStr = dformat.format(aDL.getValue());
        if (!preStr.contains("E-")) {
            preStr = preStr.replace("E", "E+");
        }
        _bw.writeBytes(GlobalUtil.padLeft(preStr, 14, ' '));

        //Write data
        _bw.write(dataBytes);
        
        return ksum;
    }

    /**
     * Write grid data
     *
     * @param time The time
     * @param levelIdx The level index
     * @param varName Variable name
     * @param forecast The forecast hour
     * @param grid The grid id
     * @param gridData The grid data
     * @throws IOException IOException
     */
    public void writeGridData(Date time, int levelIdx, String varName, int forecast, int grid, GridData gridData) throws IOException {
        DataLabel aDL = new DataLabel(time);
        aDL.setLevel(levelIdx);
        aDL.setVarName(varName);
        aDL.setGrid(grid);
        aDL.setForecast(forecast);
        writeGridData(aDL, gridData);
    }
    
    /**
     * Write grid data
     *
     * @param time The time
     * @param levelIdx The level index
     * @param varName Variable name
     * @param forecast The forecast hour
     * @param grid The grid id
     * @param gridData The grid data
     * @return Checksum
     * @throws IOException IOException
     */
    public int writeGridData(Date time, int levelIdx, String varName, int forecast, int grid, Array gridData) throws IOException {
        DataLabel aDL = new DataLabel(time);
        aDL.setLevel(levelIdx);
        aDL.setVarName(varName);
        aDL.setGrid(grid);
        aDL.setForecast(forecast);
        int ksum = writeGridData(aDL, gridData);
        return ksum;
    }

    private byte[] packARLGridData(GridData gridData, DataLabel aDL) {
        int nx = gridData.getXNum();
        int ny = gridData.getYNum();
        double var1 = gridData.data[0][0];
        double rold = var1;
        double rmax = 0.0;
        int i, j;
        //Find the maximum difference between adjacent elements
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                //Compute max difference between elements along row
                rmax = Math.max(Math.abs(gridData.data[i][j] - rold), rmax);
                rold = gridData.data[i][j];
            }
            rold = gridData.data[i][0];
        }

        double sexp = 0.0;
        //Compute the required scaling exponent
        if (rmax != 0.0) {
            sexp = Math.log(rmax) / Math.log(2.0);
        }
        int nexp = (int) sexp;
        //Positive or whole number scaling round up for lower precision
        if (sexp >= 0.0 || sexp % 1.0 == 0.0) {
            nexp += 1;
        }
        //Precision range is -127 to 127 or 254
        double prec = Math.pow(2.0, nexp) / 254.0;

        byte[] dataBytes = new byte[ny * nx];
        double SCALE = Math.pow(2.0, (7 - nexp));
        double VOLD = var1;
        double rcol = var1;
        int ksum = 0;
        int INDX = 0;
        int ival;
        for (j = 0; j < ny; j++) {
            VOLD = rcol;
            for (i = 0; i < nx; i++) {
                ival = (int) ((gridData.data[j][i] - VOLD) * SCALE + 127.5);
                dataBytes[INDX] = (byte) ival;
                VOLD = (float) (ival - 127) / SCALE + VOLD;
                if (i == 0) {
                    rcol = VOLD;
                }
                //maintain fotatin checksum
                ksum += ival;
                //if sum carries over the eight bit add one
                if (ksum >= 256) {
                    ksum = ksum - 255;
                }
                INDX += 1;
                //VOLD = gridData.Data[j, i];                                        
            }
            //VOLD = gridData.Data[j, 0];
        }

        aDL.setExponent(nexp);
        aDL.setPrecision(prec);
        aDL.setValue(var1);

        return dataBytes;
    }
    
    private Object[] packARLGridData(Array a, DataLabel aDL) {
        int nx = a.getShape()[1];
        int ny = a.getShape()[0];
        double var1 = a.getDouble(0);
        double rold = var1;
        double rmax = 0.0;
        int i, j;
        //Find the maximum difference between adjacent elements
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                //Compute max difference between elements along row
                rmax = Math.max(Math.abs(a.getDouble(i * nx + j) - rold), rmax);
                rold = a.getDouble(i * nx + j);
            }
            rold = a.getDouble(i * nx);
        }

        double sexp = 0.0;
        //Compute the required scaling exponent
        if (rmax != 0.0) {
            sexp = Math.log(rmax) / Math.log(2.0);
        }
        int nexp = (int) sexp;
        //Positive or whole number scaling round up for lower precision
        if (sexp >= 0.0 || sexp % 1.0 == 0.0) {
            nexp += 1;
        }
        //Precision range is -127 to 127 or 254
        double prec = Math.pow(2.0, nexp) / 254.0;

        byte[] dataBytes = new byte[ny * nx];
        double SCALE = Math.pow(2.0, (7 - nexp));
        double VOLD = var1;
        double rcol = var1;
        int ksum = 0;
        int INDX = 0;
        int ival;
        for (j = 0; j < ny; j++) {
            VOLD = rcol;
            for (i = 0; i < nx; i++) {
                ival = (int) ((a.getDouble(j * nx + i) - VOLD) * SCALE + 127.5);
                dataBytes[INDX] = (byte) ival;
                VOLD = (float) (ival - 127) / SCALE + VOLD;
                if (i == 0) {
                    rcol = VOLD;
                }
                //maintain fotatin checksum
                ksum += ival;
                //if sum carries over the eight bit add one
                if (ksum >= 256) {
                    ksum = ksum - 255;
                }
                INDX += 1;
                //VOLD = gridData.Data[j, i];                                        
            }
            //VOLD = gridData.Data[j, 0];
        }

        aDL.setExponent(nexp);
        aDL.setPrecision(prec);
        aDL.setValue(var1);

        return new Object[]{dataBytes, ksum};
    }
    // </editor-fold>
    // </editor-fold>
}
