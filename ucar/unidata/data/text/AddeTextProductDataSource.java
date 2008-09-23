/*
 * Copyright 1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package ucar.unidata.data.text;


import edu.wisc.ssec.mcidas.McIDASUtil;
import edu.wisc.ssec.mcidas.adde.AddeTextReader;
import edu.wisc.ssec.mcidas.adde.WxTextProduct;

import ucar.unidata.data.DataSourceDescriptor;
import ucar.unidata.metdata.NamedStationImpl;
import ucar.unidata.metdata.NamedStationTable;

import ucar.unidata.util.DateSelection;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.Misc;

import visad.*;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


/**
 * A class for handling text ADDE test
 *
 * @author IDV development team
 * @version $Revision: 1.15 $
 */
public class AddeTextProductDataSource extends NwxTextProductDataSource {

    /**
     * Default bean constructor; does nothing.
     *
     */
    public AddeTextProductDataSource() {}

    /**
     *
     * @param descriptor    descriptor for this DataSource
     * @param filename      name of the file (or URL)
     * @param properties    extra data source properties
     */
    public AddeTextProductDataSource(DataSourceDescriptor descriptor,
                                     String filename, Hashtable properties) {
        this(descriptor, Misc.newList(filename), properties);
    }


    /**
     *
     * @param descriptor    Descriptor for this DataSource
     * @param files         List of files or urls
     * @param properties    Extra data source properties
     */
    public AddeTextProductDataSource(DataSourceDescriptor descriptor,
                                     List files, Hashtable properties) {
        super(descriptor, files, "ADDE Text Products", "ADDE Text Products",
              properties);
    }

    /**
     * Read products for the station
     *
     * @param tableInfo  the table information
     * @param station  the station
     * @param dateSelection the date selection
     *
     * @return  the list of products
     */
    protected List<Product> readProducts(TableInfo tableInfo,
                                         NamedStationImpl station,
                                         DateSelection dateSelection) {
        List<Product> products = new ArrayList<Product>();
        if ( !canHandleType(tableInfo)) {
            return products;
        }
        if (station == null) {
            return products;
        }

        StringBuilder builder = new StringBuilder("adde://");
        builder.append(getDataContext().getIdv().getProperty("textserver",
                "adde.ucar.edu"));
        builder.append("/");
        builder.append(getRequest(tableInfo, station, dateSelection));


        String url = builder.toString();
        //System.out.println("url = " + url);
        try {
            AddeTextReader atr = new AddeTextReader(url);
            if (url.indexOf("wxtext") > 0) {
                List<WxTextProduct> prods = atr.getWxTextProducts();
                for (Iterator itera = prods.iterator(); itera.hasNext(); ) {
                    WxTextProduct wtp = (WxTextProduct) itera.next();
                    products.add(new Product(wtp.getWstn(), wtp.getText(),
                                             wtp.getDate()));
                }
            } else {
                String obs = atr.getText();
                products.add(new Product(station.getID(), atr.getText(),
                                         new Date()));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return products;
    }

    /**
     * Get the search string
     *
     * @param ti  table info
     * @param station  station
     * @param dateSelection the date selection
     *
     * @return the search string
     */
    private String getRequest(TableInfo ti, NamedStationImpl station,
                              DateSelection dateSelection) {

        if (station == null) {
            return "";
        }
        if (ti.flag.equals(ti.FLAG_O)) {
            return getObTextRequest(ti, station, dateSelection);
        } else {
            return getWxTextRequest(ti, station, dateSelection);
        }
    }

    /**
     * Get the weather text request
     *
     * @param ti  table info
     * @param station  station
     * @param dateSelection the date selection
     *
     * @return the request string
     */
    private String getWxTextRequest(TableInfo ti, NamedStationImpl station,
                                    DateSelection dateSelection) {
        Date[] dateRange = ((dateSelection == null)
                            ? null
                            : dateSelection.getRange());
        Date   endTime   = (dateRange == null)
                           ? new Date()
                           : dateRange[1];
        int[]  endDT = McIDASUtil.mcSecsToDayTime(endTime.getTime() / 1000);
        int    maxCount  = ((dateSelection == null)
                            ? 100
                            : dateSelection.getCount());
        maxCount = Math.min(maxCount, 100);
        int dtime = 200;
        if (dateRange != null) {
            int hours = (int) (Math.abs(dateRange[1].getTime()
                                        - dateRange[0].getTime()) / (1000
                                            * 3600));
            if (hours < 1) {
                hours = 1;
            }
            dtime = hours;
        }

        StringBuilder buf = new StringBuilder("wxtext?");
        if (ti.flag.equals(ti.FLAG_F)) {
            String afos = station.getID();
            if (afos == null || afos.equals("")) return "";
            buf.append("APRO=");
            buf.append(afos.substring(0,3));
            buf.append("&ASTN=");
            buf.append(afos.substring(3));
        } else {
            buf.append("WMO=");
            buf.append(station.getProperty(NamedStationTable.KEY_BULLETIN,
                                           "NONE"));
            buf.append("&WSTN=");
            buf.append(station.getID());
        }
        buf.append("&dtime=");
        buf.append(dtime);
        buf.append("&num=");
        buf.append(maxCount);
        buf.append("&day=");
        buf.append(endDT[0]);
        return buf.toString();
    }


    /**
     * Get the ob text request
     *
     * @param ti  table info
     * @param station  station
     * @param dateSelection the date selection
     *
     * @return  the request string
     */
    private String getObTextRequest(TableInfo ti, NamedStationImpl station,
                                    DateSelection dateSelection) {
        Date[] dateRange = ((dateSelection == null)
                            ? null
                            : dateSelection.getRange());
        Date   start     = (dateRange == null)
                           ? null
                           : dateRange[0];
        Date   end       = (dateRange == null)
                           ? null
                           : dateRange[1];
        int    maxCount  = ((dateSelection == null)
                            ? 999
                            : dateSelection.getCount());
        maxCount = Math.min(maxCount, 999);
        int hourMod = 1;

        String id = station.getID();
        String idn =
            (String) station.getProperty(NamedStationTable.KEY_IDNUMBER, "");
        if ( !idn.equals("")) {
            idn = idn.substring(0, 5);
        }
        StringBuilder buf = new StringBuilder("obtext?");
        buf.append("&descriptor=");
        if (ti.type.equals("SND_DATA")) {
            buf.append("UPPERAIR");
            id = idn;
            hourMod = 3;
        } else if (ti.type.equals("SYN_DATA")) {
            buf.append("SYNOPTIC");
            id = idn;
            hourMod = 3;
        } else if (ti.type.equals("TAFS_DEC")) {
            buf.append("TERMFCST");
        } else {  // (ti.type.equals("SFC_HRLY")) {
            buf.append("SFCHOURLY");
            // uses 3 letter ids
            if (id.length() < 4) {
                id = "K" + id;
            }
        }
        buf.append("&ID=");
        buf.append(id);
        // set the times
        // TODO:  this needs some work
        // contrary to the docs, the time in newest/oldest is HH not HHMMSS
        if (dateRange != null) {
            int[] endDT = McIDASUtil.mcSecsToDayTime(end.getTime()/1000);
            int endHour = endDT[1]/10000;
            endHour = endHour - endHour%hourMod;
            buf.append("&newest=");
            buf.append(endDT[0]);
            buf.append(" ");
            buf.append(endHour);
            int[] startDT = McIDASUtil.mcSecsToDayTime(start.getTime()/1000);
            int startHour = startDT[1]/10000;
            startHour = startHour - startHour%hourMod;
            buf.append("&oldest=");
            buf.append(startDT[0]);
            buf.append(" ");
            buf.append(startHour);
        }
        buf.append("&num=");
        buf.append(maxCount);

        return buf.toString();
    }


    /**
     * Get the table path
     * @return the base path of the data.
     */
    protected String getTablePath() {
        return getDataContext().getIdv().getProperty("tablepath", "http://www.unidata.ucar.edu/software/idv/resources");
    }

    /**
     * Set the additional resources needed for this to work.
     * @return  true if resources set okay
     */
    protected boolean setAdditionalResources() {
        return true;
    }

    /**
     * Get the error message if additional resources aren't available
     * @return  error messaage
     */
    public String getAdditionalResourcesError() {
        return "";
    }

    /**
     * Can we handle this type of data?
     *
     * @param ti  the table info
     *
     * @return true if we can handle it.
     */
    protected boolean canHandleType(TableInfo ti) {
        return ti.flag.equals(TableInfo.FLAG_B)
               || ti.flag.equals(TableInfo.FLAG_F)
               || ti.flag.equals(TableInfo.FLAG_O);
    }

}

