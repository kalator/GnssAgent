package com.example.michael.gnssagent.data_processing;

import android.location.GnssStatus;

import java.security.PrivateKey;

public class OneObs {
    /** Container for one epoch including:
     * String svid
     *
     */

    private int timeFormat = Constants.GPS_STANDARD;
    private boolean integerize = true;

    private boolean hasL1;
    private boolean hasL5;
    private boolean notDualGps;

    // first frequency
    private int satId;
    private int constellationType;
    private String svid;
    private double pseudoRange;
    private String time;
    private double accumulatedDeltaRangeMeters;
    private int accumulatedDeltaRangeState;
    private double accumulatedDeltaRangeUncertaintyMeters;
    private double carrierFrequencyHz;
    private String carrierFrequencyString;
    private double signalStrength;
    private double doppler;

    private String codeL1String;
    private String codeL5String;
    private String phaseL1String;
    private String phaseL5String;

    // second frequency
    private double pseudoRangeL5;
    private String timeL5;
    private double carrierFrequencyHzL5;
    private double accumulatedDeltaRangeMetersL5;
    private int accumulatedDeltaRangeStateL5;
    private double accumulatedDeltaRangeUncertaintyMetersL5;
    private String carrierFrequencyStringL5;
    private double signalStrengthL5;
    private double dopplerL5;

    public OneObs() {
        this.hasL1 = false;
        this.hasL5 = false;
        this.notDualGps = false;
    }

    public void setSvid(int constellationType, int svid) {
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                this.svid = "G"; // GPS
                this.codeL1String = "C1C";
                this.codeL5String = "C5X";
                this.phaseL1String = "L1C";
                this.phaseL5String = "L5X";
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                this.svid = "S"; // SBAS
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                this.svid = "R"; // Glonass
                this.codeL1String = "C1C";
                this.phaseL1String = "L1C";
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                this.svid = "J"; // QZSS
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                this.svid = "C"; // Beidou
                this.codeL1String = "C1C";
                this.phaseL1String = "L1C";
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                this.svid = "E"; // Gallileo
                this.codeL1String = "C1C";
                this.codeL5String = "C5X";
                this.phaseL1String = "L1C";
                this.phaseL5String = "L5X";
                break;
            default:
                this.svid =  "u"; // Unknown
                break;
        }
        if (svid < 10) {
            this.svid += "0";
        }
        this.svid += svid;

        this.satId = svid;
        this.constellationType = constellationType;
    }

    public String getSvid() {
        return this.svid;
    }

    public void setPseudoRange (double pseudoRange, String fq) {
        if (fq.equals("L1") || fq.equals("E1") || fq.equals("NonDual")) {
            this.pseudoRange = pseudoRange;
        } else if (fq.equals("L5") || fq.equals("E5")) {
            this.pseudoRangeL5 = pseudoRange;
        }

    }


    public void setTime(double gpsNanos, String fq) {

        if(fq.equals("L1") || fq.equals("E1") || fq.equals("NonDual")) {
            setTimeL1(gpsNanos);
        } else {
            if(fq.equals("L5") || fq.equals("E5")) {
                setTimeL5(gpsNanos);
            }
        }
    }

    public void setTimeL1 (double gpsNanos) {
        int precision = integerize ? 0 : 7;
        if (timeFormat == Constants.GPS_WEEK_SECONDS) {
            this.time = TimeConverter.gpsNanos2gpsWeekSeconds((long) gpsNanos, precision);
        } else {
            if (timeFormat == Constants.GPS_STANDARD) {
                this.time = TimeConverter.gpsNanos2gpsStandard((long) gpsNanos, precision);
            }
        }
    }

    public void setTimeL5(double gpsNanos) {
        int precision = integerize ? 0 : 7;
        if (timeFormat == Constants.GPS_WEEK_SECONDS) {
            this.timeL5 = TimeConverter.gpsNanos2gpsWeekSeconds((long) gpsNanos, precision);
        } else {
            if (timeFormat == Constants.GPS_STANDARD) {
                this.timeL5 = TimeConverter.gpsNanos2gpsStandard((long) gpsNanos, precision);
            }
        }
    }

    public String getTime() {
        return time;
    }

    public void setAccumulatedDeltaRangeMeters(double accumulatedDeltaRangeMeters, String fq) {
        if (fq.equals("L1") || fq.equals("E1")  || fq.equals("NonDual")) {
            this.accumulatedDeltaRangeMeters = accumulatedDeltaRangeMeters;
        } else {
            if(fq.equals("L5") || fq.equals("E5")) {
                this.accumulatedDeltaRangeMetersL5 = accumulatedDeltaRangeMeters;
            }

        }
    }

    public void setAccumulatedDeltaRangeState(int accumulatedDeltaRangeState, String fq) {
        if (fq.equals("L1") || fq.equals("E1") || fq.equals("NonDual")) {
            this.accumulatedDeltaRangeState = accumulatedDeltaRangeState;
        } else {
            if (fq.equals("L5") || fq.equals("E5")) {
                this.accumulatedDeltaRangeState = accumulatedDeltaRangeState;
            }
        }
    }

    public void setAccumulatedDeltaRangeUncertaintyMeters(
            double accumulatedDeltaRangeUncertaintyMeters, String fq) {
        if (fq.equals("L1") || fq.equals("E1")  || fq.equals("NonDual")) {
            this.accumulatedDeltaRangeUncertaintyMeters = accumulatedDeltaRangeUncertaintyMeters;
        } else {
            if (fq.equals("L5") || fq.equals("E5")) {
                this.accumulatedDeltaRangeUncertaintyMetersL5 = accumulatedDeltaRangeUncertaintyMeters;
            }

        }
    }

    public void setCarrierFrequencyHz(double carrierFrequencyHz, String fq) {
        if (fq.equals("L1") || fq.equals("E1")  || fq.equals("NonDual")) {
            this.carrierFrequencyHz = carrierFrequencyHz;
        } else {
            if (fq.equals("L5") || fq.equals("E5")) {
                this.carrierFrequencyHzL5 = carrierFrequencyHz;
            }

        }
    }

    public int getConstellationType() {
        return constellationType;
    }

    public int getSatId() {
        return satId;
    }

    public void setFqBands(String what) {
        if (what.equals("L1") || what.equals("E1")) {
            this.hasL1 = true;
        } else {
            if (what.equals("L5") || what.equals("E5")) {
                this.hasL5 = true;
            } else {
                if (what.equals("NonDual")) {
                    this.notDualGps = true;
                }
            }
        }
    }

    public void setSignalStrength(double signalStrength, String fq) {
        if (fq.equals("L1") || fq.equals("E1")  || fq.equals("NonDual")) {
            this.signalStrength = signalStrength;
        } else {
            if (fq.equals("L5") || fq.equals("E5")) {
                this.signalStrengthL5 = signalStrength;
            }

        }
    }

    public void setDoppler(double doppler, String fq) {
        if (fq.equals("L1") || fq.equals("E1") || fq.equals("NonDual")) {
            this.doppler = doppler;
        } else {
            if (fq.equals("L5") || fq.equals("E5")) {
                this.dopplerL5 = doppler;
            }
        }
    }

    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    public void setIntegerize(boolean integerize) {
        this.integerize = integerize;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str
                .append("GEOP ")
                .append(svid);

        if(notDualGps) {
            str
                    .append(" "+codeL1String)
                    .append(String.format(" %.3f", pseudoRange))
                    .append("\t"+phaseL1String)
                    .append(String.format(" %.3f",accumulatedDeltaRangeMeters))
                    .append("\t"+accumulatedDeltaRangeState)
                    .append(String.format("\t D1C %.3f", doppler))
                    .append(String.format("\t S1C %.3f", signalStrength));
        }

        if (hasL1) {
            str
                    .append(" "+codeL1String)
                    .append(String.format(" %.3f", pseudoRange))
                    .append("\t"+phaseL1String)
                    .append(String.format(" %.3f",accumulatedDeltaRangeMeters))
                    .append("\t"+accumulatedDeltaRangeState)
                    .append(String.format("\t D1C %.3f", doppler))
                    .append(String.format("\t S1C %.3f", signalStrength));
        }

        if (hasL5) {
            str
                    .append(" "+codeL5String)
                    .append(String.format(" %.3f", pseudoRangeL5))
                    .append("\t"+phaseL5String)
                    .append(String.format(" %.3f",accumulatedDeltaRangeMetersL5))
                    .append("\t"+accumulatedDeltaRangeStateL5)
                    .append(String.format("\t D5X %.3f", dopplerL5))
                    .append(String.format("\t S5X %.3f", signalStrengthL5));
        }

        return str.toString();
    }
}
