/*
 *
 * Copyright  1997-2012 Unidata Program Center/University Corporation for
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



package ucar.unidata.view.geoloc;

//~--- non-JDK imports --------------------------------------------------------

import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.LogUtil;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * A widget to get lat/lon range info from the user
 *
 * @author   IDV Development Team
 */
public class LatLonScaleDialog extends JPanel implements ActionListener {

    /** Abscissa (x-axis) Label */
    private JTextField abscissaLabel;

    /** Latitude base label */
    private JTextField latBaseLabel;

    /** Latitude increment */
    private JTextField latIncrement;

    /** Lat lon scale info */
    private LatLonScaleInfo latLonScaleInfo;

    /** Latitude minor increment */
    private JSpinner latMinorSpinner;

    /** Longitude base label */
    private JTextField lonBaseLabel;

    /** Longitude increment */
    private JTextField lonIncrement;

    /** Longitude minor increment */
    private JSpinner             lonMinorSpinner;
    private MapProjectionDisplay mpDisplay;

    /** flag for whether the user hit cancel or not */
    private boolean ok;

    /** Ordinate (y-axis) Label */
    private JTextField ordinateLabel;

    /** The frame parent */
    JFrame parent;

    /** x axis visible */
    private JCheckBox xVisible;

    /** y axis visible */
    private JCheckBox yVisible;

    /**
     * Create a new dialog for setting the coordinate range of the display
     *
     */
    public LatLonScaleDialog(MapProjectionDisplay mpDisplay) {
        this.mpDisplay       = mpDisplay;
        this.parent          = GuiUtils.getFrame(mpDisplay.getComponent());
        this.latLonScaleInfo = mpDisplay.getLatLonScaleInfo();
        doMakeContents();
    }

    /**
     * Make the widget contents (UI)
     */
    protected void doMakeContents() {
        setLayout(new BorderLayout());
        GuiUtils.tmpInsets = new Insets(5, 5, 0, 0);

        JPanel p1 = GuiUtils.doLayout(new Component[] {
            GuiUtils.rLabel("Abscissa (x-axis) Label: "), abscissaLabel = new JTextField(""),
            GuiUtils.rLabel("Ordinate (y-axis) Label: "), ordinateLabel = new JTextField(""),
            GuiUtils.rLabel("Latitude Base: "), latBaseLabel = new JTextField(""), GuiUtils.rLabel("Longitude Base: "),
            lonBaseLabel = new JTextField(""), GuiUtils.rLabel("Latitude Increment: "),
            latIncrement = new JTextField(""), GuiUtils.rLabel("Latitude Minor Increment: "),
            latMinorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1)),
            GuiUtils.rLabel("Longitude Increment: "), lonIncrement = new JTextField(""),
            GuiUtils.rLabel("Longitude Minor Increment: "),
            lonMinorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1)),
            GuiUtils.rLabel("Abscissa (x-axis) Visible: "), xVisible = new JCheckBox("", true),
            GuiUtils.rLabel("Ordinate (y-axis) Visible: "), yVisible = new JCheckBox("", true)
        }, 2, GuiUtils.WT_NY, GuiUtils.WT_N);

        this.add("Center", GuiUtils.inset(p1, 5));

        if (latLonScaleInfo != null) {
            populateLatLonScaleInfo();
        }
    }

    /**
     * Handle user click on OK or other(cancel) button.  Closes the
     * dialog.
     *
     * @param evt  event to handle
     */
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();

        if (cmd.equals(GuiUtils.CMD_OK)) {
            if (!doApply()) {
                return;
            }

            setVisible(false);
        } else if (cmd.equals(GuiUtils.CMD_APPLY)) {
            doApply();
        } else if (cmd.equals(GuiUtils.CMD_CANCEL)) {
            setVisible(false);
        }
    }

    /**
     * Populate lat lon scale info.
     */
    private void populateLatLonScaleInfo() {
        abscissaLabel.setText(this.latLonScaleInfo.abscissaLabel);
        ordinateLabel.setText(this.latLonScaleInfo.ordinateLabel);
        latBaseLabel.setText(this.latLonScaleInfo.latBaseLabel);
        lonBaseLabel.setText(this.latLonScaleInfo.lonBaseLabel);
        latIncrement.setText(this.latLonScaleInfo.latIncrement);
        latMinorSpinner.setValue(this.latLonScaleInfo.latMinorIncrement);
        lonIncrement.setText(this.latLonScaleInfo.lonIncrement);
        lonMinorSpinner.setValue(this.latLonScaleInfo.lonMinorIncrement);
        xVisible.setSelected(this.latLonScaleInfo.xVisible);
        xVisible.setSelected(this.latLonScaleInfo.yVisible);
    }

    /**
     * Apply the dialog state
     *
     * @return Was it successful
     */
    public boolean doApply() {
        LatLonScaleInfo newLatLonInfo = new LatLonScaleInfo();

        newLatLonInfo.abscissaLabel     = abscissaLabel.getText();
        newLatLonInfo.ordinateLabel     = ordinateLabel.getText();
        newLatLonInfo.latBaseLabel      = latBaseLabel.getText();
        newLatLonInfo.lonBaseLabel      = lonBaseLabel.getText();
        newLatLonInfo.latIncrement      = latIncrement.getText();
        newLatLonInfo.latMinorIncrement = Integer.valueOf(latMinorSpinner.getValue().toString());
        newLatLonInfo.lonIncrement      = lonIncrement.getText();
        newLatLonInfo.lonMinorIncrement = Integer.valueOf(lonMinorSpinner.getValue().toString());
        newLatLonInfo.xVisible          = xVisible.isSelected();
        newLatLonInfo.yVisible          = yVisible.isSelected();

        if (!newLatLonInfo.equals(latLonScaleInfo)) {
            latLonScaleInfo = newLatLonInfo;
        }

        mpDisplay.setDisplayInactive();

        try {
            mpDisplay.setLatLonScaleInfo(latLonScaleInfo);
            mpDisplay.setDisplayActive();
        } catch (Exception e) {
            LogUtil.userMessage("An error has occurred:" + e);

            return false;
        }

        return true;
    }

    public void setLatLonScaleInfo(LatLonScaleInfo latLonScaleInfo) {
        this.latLonScaleInfo = latLonScaleInfo;
    }
}