package com.example.michael.gnssagent.data_processing;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;

import static com.example.michael.gnssagent.data_processing.Constants.EPS_HZ_GPS;
import static com.example.michael.gnssagent.data_processing.Constants.GALILEO_L1_FREQUENCY_HZ;
import static com.example.michael.gnssagent.data_processing.Constants.GALILEO_L5_FREQUENCY_HZ;
import static com.example.michael.gnssagent.data_processing.Constants.GLONASS_L1_FREQUENCY_HZ_AVG;
import static com.example.michael.gnssagent.data_processing.Constants.GPS_L1_FREQUENCY_HZ;
import static com.example.michael.gnssagent.data_processing.Constants.GPS_L5_FREQUENCY_HZ;
import static com.example.michael.gnssagent.data_processing.Constants.NUMBER_NANO_SECONDS_100_MILLI;
import static com.example.michael.gnssagent.data_processing.Constants.NUMBER_NANO_SECONDS_DAY;
import static com.example.michael.gnssagent.data_processing.Constants.NUMBER_NANO_SECONDS_WEEK;
import static com.example.michael.gnssagent.data_processing.Constants.SPEED_OF_LIGHT;
import static com.example.michael.gnssagent.data_processing.Constants.UTC_TAI_LEAP_SECONDS;

public class OneEpoch {

    private Collection<GnssMeasurement> gnssMeasurements;
    private GnssClock clock;
    private ArrayList<OneObs> oneEpochObs;
    private VisibleUsableSatelites sats;
    private static boolean pseudoRangeCompIsOn = false; //  we need time only from first epoch
    // (according to GSA white paper)
    private static long fullBiasNanos;
    private static int hwClockDiscontinuity = 0;

    private int timeFormat;

    private boolean integerizeTime;

    // constructor
    public OneEpoch(GnssMeasurementsEvent eventArgs, int timeFormat, boolean integerizeTime) {
        this.integerizeTime = integerizeTime;
        this.timeFormat = timeFormat;
        sats = new VisibleUsableSatelites();
        this.gnssMeasurements = eventArgs.getMeasurements();

        // TODO: how to know, what constellation is used for time estimate
        this.clock = eventArgs.getClock();

        oneEpochObs = new ArrayList<>();

        determineConst();
    }

    // setters, getters
    public ArrayList<OneObs> getOneEpochObs() {
        return oneEpochObs;
    }

    public VisibleUsableSatelites getSats() {
        return sats;
    }

    public void setFullBiasNanos() {
        // we need these only from first epoch (according to GSA whitepaper)
        // TODO: Check this!
        if (clock.hasFullBiasNanos()) {
            if (!pseudoRangeCompIsOn
                    || (clock.getHardwareClockDiscontinuityCount() != hwClockDiscontinuity)) {
                fullBiasNanos = clock.getFullBiasNanos();
                pseudoRangeCompIsOn = true;
            }
        }

    }

    protected void determineConst() {

        double biasNanos = clock.getBiasNanos();
        long  timeNanos = clock.getTimeNanos();

        double gpsTimeEpoch = timeNanos -
                (fullBiasNanos + biasNanos); // ns

        int i = 0;
        for (GnssMeasurement meas : gnssMeasurements) {

            // compute time
            long tTx = meas.getReceivedSvTimeNanos(); // ns, time of receiving
            double tRxGNSS = gpsTimeEpoch + meas.getTimeOffsetNanos(); // ns


            // GPS
            if (meas.getConstellationType() == GnssStatus.CONSTELLATION_GPS) {
                processGps(meas, tTx, tRxGNSS);

            }

            // Galileo
            else if (meas.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO) {
                processGalileo(meas, tTx, tRxGNSS);
            }

            // GLONASS
            else if (meas.getConstellationType() == GnssStatus.CONSTELLATION_GLONASS) {
                processGlonass(meas, tTx, tRxGNSS);
            }

            // BeiDou
            else if (meas.getConstellationType() == GnssStatus.CONSTELLATION_BEIDOU) {
                processBeidou(meas, tTx, tRxGNSS);
            }

            // SBAS
            else if (meas.getConstellationType() == GnssStatus.CONSTELLATION_SBAS) {
                processSbas(meas, tTx, tRxGNSS);
            }

            // QZSS
            else if (meas.getConstellationType() == GnssStatus.CONSTELLATION_QZSS) {
                processQzss(meas, tTx, tRxGNSS);
            }

            i++;
        }
    }

    private void processGps(GnssMeasurement meas, long tTx, double tRxGNSS) {

        sats.gpsVisible++;

        boolean codeLock = (meas.getState() & GnssMeasurement.STATE_CODE_LOCK) > 0;
        boolean towDecoded = (meas.getState() & GnssMeasurement.STATE_TOW_DECODED) > 0;
        boolean towUncertainty = meas.getReceivedSvTimeUncertaintyNanos() < 50;

        if (codeLock && towDecoded && towUncertainty) {

            setFullBiasNanos();
            double weekNumberNanos = Math.floor(-1.*fullBiasNanos/NUMBER_NANO_SECONDS_WEEK)
                    * NUMBER_NANO_SECONDS_WEEK; // ns
            double pseudoRange = (tRxGNSS - weekNumberNanos - tTx) * SPEED_OF_LIGHT / 1.0e9; // m

            processBase(pseudoRange, meas, tRxGNSS, "GPS");

        }
    }


    private void processGalileo(GnssMeasurement meas, long tTx, double tRxGNSS) {

        sats.galileoVisible++;

        double pseudoRange = 1e10;

        boolean codeLock = (meas.getState() & GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK) > 0;
        boolean towDecoded = (meas.getState() & GnssMeasurement.STATE_TOW_DECODED) > 0;
        // boolean towUncertainty = meas.getReceivedSvTimeUncertaintyNanos() < 50;
        boolean towKnown = (meas.getState() & GnssMeasurement.STATE_TOW_KNOWN) > 0;

        if (towKnown || towDecoded) {
            setFullBiasNanos();
            double weekNumberNanos = Math.floor(-fullBiasNanos/NUMBER_NANO_SECONDS_WEEK)
                    * NUMBER_NANO_SECONDS_WEEK; // ns
            pseudoRange = (tRxGNSS - weekNumberNanos - tTx) * SPEED_OF_LIGHT / 1.0e9; // m;
        } else {
            if (codeLock) {
                setFullBiasNanos();
                double milliSecondsNumberNanos = Math.floor(-fullBiasNanos/NUMBER_NANO_SECONDS_100_MILLI)
                        * NUMBER_NANO_SECONDS_100_MILLI; // ns
                pseudoRange = (tRxGNSS - milliSecondsNumberNanos - tTx) * SPEED_OF_LIGHT / 1.0e9; // m;
            }
        }

        processBase(pseudoRange, meas, tRxGNSS, "GALILEO");

    }


    private void processGlonass(GnssMeasurement meas, long tTx, double tRxGNSS) {
        sats.glonassVisible++;

        // check if sat number is decoded, if not, skip
        if (meas.getSvid() >= 93 && meas.getSvid() <= 106) {
            return;
        }

        boolean codeLock = (meas.getState() & GnssMeasurement.STATE_CODE_LOCK) > 0;
        boolean todDecoded = (meas.getState() & GnssMeasurement.STATE_GLO_TOD_DECODED) > 0;
        boolean todKnown = (meas.getState() & GnssMeasurement.STATE_GLO_TOD_KNOWN) > 0;


        if (codeLock /*&& todDecoded*/ && todKnown) {

            setFullBiasNanos();

            double dayNumberNanos = Math.floor(-fullBiasNanos/NUMBER_NANO_SECONDS_DAY)*NUMBER_NANO_SECONDS_DAY; //ns

            double tRx = tRxGNSS-dayNumberNanos + 3*3600*1e9 - (UTC_TAI_LEAP_SECONDS-19)*1e9;

            double pseudoRange = (tRx - tTx) * SPEED_OF_LIGHT / 1.0e9; // m

            processBase(pseudoRange, meas, tRxGNSS, "GLONASS");

        }
    }

    private void processBeidou(GnssMeasurement meas, long tTx, double tRxGNSS) {
        sats.beidouVisible++;

        boolean codeLock = (meas.getState() & GnssMeasurement.STATE_CODE_LOCK) > 0;
        boolean towDecoded = (meas.getState() & GnssMeasurement.STATE_TOW_DECODED) > 0;
//        boolean towUncertainty = meas.getReceivedSvTimeUncertaintyNanos() < 50;

        if (codeLock && towDecoded) {

            setFullBiasNanos();
            double weekNumberNanos = Math.floor(-1.*fullBiasNanos/NUMBER_NANO_SECONDS_WEEK)
                    * NUMBER_NANO_SECONDS_WEEK; // ns
            double pseudoRange = (tRxGNSS - weekNumberNanos - 14000000000L - tTx) * SPEED_OF_LIGHT / 1.0e9; // m

            processBase(pseudoRange, meas, tRxGNSS, "BDS");

        }
    }

    private void processBase(double pseudoRange, GnssMeasurement meas, double tRxGNSS, String gnssSys) {

        double hzL1 = 0;
        double hzL5 = 0;
        if (gnssSys.equals("GPS")) {
            hzL1 = GPS_L1_FREQUENCY_HZ;
            hzL5 = GPS_L5_FREQUENCY_HZ;
        } else if (gnssSys.equals("GALILEO")) {
            hzL1 = GALILEO_L1_FREQUENCY_HZ;
            hzL5 = GALILEO_L5_FREQUENCY_HZ;
        } else if (gnssSys.equals("GLONASS")) {
            hzL1 = GLONASS_L1_FREQUENCY_HZ_AVG;
        }
        // is pseudorange usable and did we set fullbiasnanos?
        // TODO: rename pseudoRangeCompIsOn flag
        if (Math.abs(pseudoRange) < 1e9 && pseudoRangeCompIsOn) {
            // is sat already observed? If yes, add observation to decoded satellite
            for (OneObs obs : oneEpochObs) {
                if (meas.getConstellationType() == obs.getConstellationType() &&
                        meas.getSvid() == obs.getSatId()) {

                    if(gnssSys.equals("GPS")){
                        sats.gpsVisible--; // this satellite is already included
                    } else if (gnssSys.equals("GALILEO")) {
                        sats.galileoVisible--;
                    }

                    String fq = "";
                    if (Math.abs(meas.getCarrierFrequencyHz() - hzL1)
                            < EPS_HZ_GPS) {

                        if(gnssSys.equals("GPS")){
                            sats.gpsL1++;
                            fq = "L1";
                        } else if (gnssSys.equals("GALILEO")) {
                            sats.galileoE1++;
                            fq = "E1";
                        }

                    }
                    else {
                        if (Math.abs(meas.getCarrierFrequencyHz() - hzL5)
                                < EPS_HZ_GPS) {

                            if(gnssSys.equals("GPS")){
                                sats.gpsL5++;
                                fq = "L5";
                            } else if (gnssSys.equals("GALILEO")) {
                                sats.galileoE5++;
                                fq = "E5";
                            }
                        }
                    }

                    setOneObsParams(obs, meas, tRxGNSS, pseudoRange, fq, gnssSys);
                    return;
                }
            }

            // satellite is not included yet
            OneObs oneObs = new OneObs();
            oneObs.setTimeFormat(timeFormat);
            oneObs.setIntegerize(integerizeTime);

            if(gnssSys.equals("GPS")){
                sats.gpsUsable++;
            } else if (gnssSys.equals("GALILEO")) {
                sats.galileoUsable++;
            } else if (gnssSys.equals("GLONASS")) {
                sats.glonassUsable++;
            } else if (gnssSys.equals("BDS")) {
                sats.beidouUsable++;
            }


            sats.usableInTotal++;
            oneObs.setSvid(meas.getConstellationType(), meas.getSvid());

            // TODO: Better checking whether mobile has dual gps
            String fq = "";
            if (Math.abs(meas.getCarrierFrequencyHz() - hzL1)
                    < EPS_HZ_GPS) {

                if(gnssSys.equals("GPS")){
                    sats.gpsL1++;
                    fq = "L1";
                } else if (gnssSys.equals("GALILEO")) {
                    sats.galileoE1++;
                    fq = "E1";
                }


            } else if (Math.abs(meas.getCarrierFrequencyHz() - hzL5)
                    < EPS_HZ_GPS) {

                if(gnssSys.equals("GPS")){
                    sats.gpsL5++;
                    fq = "L5";
                } else if (gnssSys.equals("GALILEO")) {
                    sats.galileoE5++;
                    fq = "E5";
                }

            } else if (!meas.hasCarrierFrequencyHz()) {
                fq = "NonDual";

            } else if (gnssSys.equals("GLONASS")) {
                fq = "L1";
            } else if (gnssSys.equals("BDS")) {
                fq = "L1";
            }
            setOneObsParams(oneObs, meas, tRxGNSS, pseudoRange, fq, gnssSys);
            oneEpochObs.add(oneObs);
        } else {
            pseudoRangeCompIsOn = false;
        }
    }

    private void setOneObsParams(OneObs obs, GnssMeasurement meas,
                                 double tRxGNSS, double pseudoRange,
                                 String fq, String gnssSys) {

        double wavelength = SPEED_OF_LIGHT/meas.getCarrierFrequencyHz();
        if (fq.equals("NonDual"))
        {
            if(gnssSys.equals("GPS")){
                wavelength = SPEED_OF_LIGHT/GPS_L1_FREQUENCY_HZ;
            } else if (gnssSys.equals("GALILEO")) {
                wavelength = SPEED_OF_LIGHT/GALILEO_L1_FREQUENCY_HZ;
            }

        }

        if(fq.equals("NonDual")) {
            sats.satStrengthList.add(Pair.create(obs.getSvid(),
                    meas.getCn0DbHz()));
        } else {
            sats.satStrengthList.add(Pair.create(obs.getSvid()+"_"+fq,
                    meas.getCn0DbHz()));
        }

        long gpsSecondsRound = 0L;
        long fracTime;
        if (integerizeTime) {
            Double gpsSecondsUp = tRxGNSS/1e9 + 0.5;
            gpsSecondsRound = 1000000000L*gpsSecondsUp.intValue();
            fracTime = (long)tRxGNSS- gpsSecondsRound;
            pseudoRange -= fracTime*SPEED_OF_LIGHT/1e9;
        }
        obs.setTime(integerizeTime ? gpsSecondsRound : tRxGNSS, fq);
        obs.setPseudoRange(pseudoRange, fq);
        obs.setAccumulatedDeltaRangeMeters(
                meas.getAccumulatedDeltaRangeMeters()/wavelength, fq);
        obs.setAccumulatedDeltaRangeState(
                meas.getAccumulatedDeltaRangeState(), fq);
        obs.setAccumulatedDeltaRangeUncertaintyMeters(
                meas.getAccumulatedDeltaRangeUncertaintyMeters(), fq);
        obs.setCarrierFrequencyHz(meas.getCarrierFrequencyHz(), fq);
        obs.setSignalStrength(meas.getCn0DbHz(), fq);
        obs.setDoppler(-meas.getPseudorangeRateMetersPerSecond() / wavelength, fq);
        obs.setFqBands(fq); // must be after setPseudorange for OneObs.toString() method

    }

    private void processSbas(GnssMeasurement meas, long tTx, double tRxGNSS) {
        sats.sbasVisible++;
    }

    private void processQzss(GnssMeasurement meas, long tTx, double tRxGNSS) {
        sats.qzssVisible++;
    }

    public static void resetClass() {
        pseudoRangeCompIsOn = false;
    }
}