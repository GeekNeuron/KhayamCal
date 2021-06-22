/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cepmuvakkit.times.posAlgo

class SolarPosition {
    private val SUNRADIUS = 0.26667
    private val FAJR: Byte = 0
    private val SUNRISE: Byte = 1
    private val SUNTRANSIT: Byte = 2
    private val ASR_SHAFI: Byte = 3
    private val ASR_HANEFI: Byte = 4
    private val SUNSET: Byte = 5
    private val ISHA: Byte = 6
    private val SUN_COUNT: Byte = 7
    private val FAJR_: Byte = 0
    private val ISRAK: Byte = 1
    private val SUNTRANSIT_: Byte = 2
    private val ASRHANEFI: Byte = 3
    private val ISFIRAR: Byte = 4
    private val SUNSET_: Byte = 5
    private val KERAHAT_COUNT: Byte = 6
    private val DUHA: Byte = 7
    private val ISTIVA: Byte = 8

    /**
     * If its absolute value is greater than 20 minutes,
     * by adding or subtracting 1440.
     *
     * @param minutes
     * @return limitminutes
     */
    fun limitMinutes(minutes: Double): Double {
        var limited = minutes
        if (limited < -20.0) {
            limited += 1440.0
        } else if (limited > 20.0) {
            limited -= 1440.0
        }
        return limited
    }

    fun limitDegrees180pm(degrees: Double): Double {
        var degrees = degrees
        var limited: Double
        degrees /= 360.0
        limited = 360.0 * (degrees - Math.floor(degrees))
        if (limited < -180.0) {
            limited += 360.0
        } else if (limited > 180.0) {
            limited -= 360.0
        }
        return limited
    }

    fun limitDegrees180(degrees: Double): Double {
        var degrees = degrees
        var limited: Double
        degrees /= 180.0
        limited = 180.0 * (degrees - Math.floor(degrees))
        if (limited < 0) {
            limited += 180.0
        }
        return limited
    }

    fun limitZero2one(value: Double): Double {
        var limited: Double
        limited = value - Math.floor(value)
        if (limited < 0) {
            limited += 1.0
        }
        return limited
    }

    fun dayFracToLocalHour(dayfrac: Double, timezone: Double): Double {
        return 24.0 * limitZero2one(dayfrac + timezone / 24.0)
    }

    /**
     * Calculate the Earth heliocentric longitude, L in degrees,
     *  “Heliocentric” means that the Earth position is calculated
     * with respect to the center of the sun
     *   L0i = Ai *cos ( Bi + Ci* JME )
     *   L0=ΣL0i (0,n)
     * where,- i is the ith row for the term L0, n is the νmber of rows for the term L0.
     *  Ai , Bi , and Ci are the values in the ith row and A, B, and C columns
     *  L =  L0 + L1* JME + L2* JME^2 + L3* JME^3 + L4* JME ^4+ L5* JME^5/10^8
     *  L (in degrees) = L(radians)*180/pi
     *
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth heliocentric longitude, L in degrees.
     */
    private fun earthHeliocentricLongitude(jme: Double): Double {
        L_COUNT = LTERMS.size.toByte()
        val sum = DoubleArray(L_COUNT.toInt())
        var i: Int
        i = 0
        while (i < L_COUNT) {
            sum[i] = earthPeriodicTermSummation(LTERMS[i], LTERMS[i].length, jme)
            i++
        }
        return limitDegrees(Math.toDegrees(earthValues(sum, L_COUNT.toInt(), jme)))
    }

    /**
     * Calculate the Earth radius vector, R (in Astronomical Units, AU),
     * Note that there is no R5, consequently, replace it by zero.
     * “Heliocentric” means that the Earth position is calculated with respect to the center of the sun
     *       R = (R0 + R1* JME + R2* JME^2 + R3* JME^3 + R4* JME ^4)/10^8
     *
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth radius vector, R (in Astronomical Units, AU),
     */
    private fun earthRadiusVector(jme: Double): Double {
        R_COUNT = RTERMS.size.toByte()
        val sum = DoubleArray(R_COUNT.toInt())
        var i: Int
        i = 0
        while (i < R_COUNT) {
            sum[i] = earthPeriodicTermSummation(RTERMS[i], RTERMS[i].length, jme)
            i++
        }
        return earthValues(sum, R_COUNT.toInt(), jme)
    }

    /**
     * Calculate the Earth heliocentric latitude, B (in degrees),
     *  “Heliocentric” means that the Earth position is calculated with respect to the center of the sun
     *  B=(B0 + B1* JME )/10^8
     *  B (in degrees) = B(radians)*180/pi
     *
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the Earth heliocentric latitude, B (in degrees).
     */
    private fun earthHeliocentricLatitude(jme: Double): Double {
        B_COUNT = BTERMS.size.toByte()
        val sum = DoubleArray(B_COUNT.toInt())
        var i: Int
        i = 0
        while (i < B_COUNT) {
            sum[i] = earthPeriodicTermSummation(BTERMS[i], BTERMS[i].length, jme)
            i++
        }
        return Math.toDegrees(earthValues(sum, B_COUNT.toInt(), jme))
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /// Calculate the Earth heliocentric longitude, latitude, and radius vector (L, B, and R): END  ///
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Calculate L0i = Ai *cos ( Bi + Ci* JME )
     *  L0=ΣL0i (0,n)
     * where,- i is the ith row for the term L0 in Table A4.2, n is the νmber of rows for the term L0
     *  Ai , Bi , and Ci are the values in the ith row and A, B, and C columns in Table A4.2, for the term L0 (in radians)
     *
     * @param jme   the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @param terms LTERMS, BTERMS,RTERMS
     * @return L0i = Ai *cos ( Bi + Ci* JME )
     */
    private fun earthPeriodicTermSummation(
        terms: Array<DoubleArray>,
        count: Int,
        jme: Double
    ): Double {
        var i: Int
        var sum = 0.0
        i = 0
        while (i < count) {
            sum += terms[i][0] * Math.cos(terms[i][1] + terms[i][2] * jme)
            i++
        }
        return sum
    }

    /**
     * Calculate the Earth Values, L, R and B.
     * replacing all the Ls by Bs  and Rs in all equations.
     * L=(L0 + L1* JME + L2* JME^2 + L3* JME^3 + L4* JME ^4+ L5* JME^5)/ 10^8
     *
     * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
     * @return the L,B and R.
     */
    private fun earthValues(termSum: DoubleArray, count: Int, jme: Double): Double {
        var i: Int
        var sum = 0.0
        i = 0
        while (i < count) {
            sum += termSum[i] * MATH.pow(jme, i.toDouble())
            i++
        }
        sum /= 1.0e8
        return sum
    }

    /**
     * Calculate the geocentric latitude, β (in degrees),
     *  β= − B
     *
     * @param b is the  geocentric latitude
     * @return β the geocentric latitude (in degrees)
     */
    fun getGeocentricLatitude(b: Double): Double {
        return -b
    }

    /**
     * Calculate the geocentric longitude, (in degrees),
     * Geocentric” means that the sun position is calculated with respect to the Earth center.
     * Θ= L + 180
     * Limit L to the range from 0 to 360. That can be accomplished
     * by dividing L by 360 and recording the decimal fraction of the division as F.
     *
     *  If L is positive, then the limited L = 360 * F. If L is negative, then the limited L = 360 - 360 * F.
     *
     * @param L Earth heliocentric longitude,
     * @return the geocentric longitude Θ (in degrees)
     */
    fun geocentricLongitude(L: Double): Double {
        var theta = L + 180.0
        if (theta >= 360.0) {
            theta -= 360.0
        }
        return theta
    }

    /**
     * Calculate the apparent sun longitude, λ (in degrees):
     *  λ=Θ+Δψ+Δτ
     *
     * @param Δτ the aberration correction Δτ (in degrees).
     * @param Δψ the nutation in longitude Δψ (in degrees).
     * @param Θ  the geocentric longitude Θ (in degrees).
     * @return the apparent sun longitude, λ (in degrees).
     */
    private fun apparentSunLongitude(Θ: Double, Δψ: Double, Δτ: Double): Double {
        return Θ + Δψ + Δτ
    }

    /**
     * Calculate the aberration correction Δτ (in degrees):
     *  Δτ=-20.4898/3600R
     *
     * @param r the Earth radius vector, R (in Astronomical Units, AU).
     * @return the aberration correction Δτ (in degrees).
     */
    private fun aberrationCorrection(r: Double): Double {
        return -20.4898 / (3600.0 * r)
    }

    /**
     * Calculate the mean sidereal time at Greenwich, ν0 (in degrees),:
     *  ν0=280.46061837+ 360.98564736629 *(JD −2451545)+0.000387933*JC^2−JC^3/38710000
     *
     * @param jd is the Julian Day
     * @param jc the Julian  Century
     * @return the mean  sidereal time at Greenwich, ν0 (in degrees)
     */
    fun greenwichMeanSiderealTime(jd: Double, jc: Double): Double {
        return limitDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) + jc * jc * (0.000387933 - jc / 38710000.0))
    }

    /**
     * Calculate the Geometric Mean Longitude of the Sun.
     * This value is close to 0? at the spring equinox,
     * 90? at the summer solstice, 180? at the automne equinox
     * and 270? at the winter solstice.
     *
     * @param jme JME is the Julian Ephemeris Millennium.
     * @return the  Sun mean longitude.
     */
    fun sunMeanLongitude(jme: Double): Double {
        return limitDegrees(
            280.4664567 + jme * (360007.6982779 + jme * (0.03032028
                    + jme * (1 / 49931.0 + jme * (-1 / 15300.0 + jme * (-1 / 2000000.0)))))
        )
    }

    fun observerHourAngle(ν: Double, longitude: Double, αDeg: Double): Double {
        return limitDegrees(ν + longitude - αDeg)
    }

    fun sunEquatorialHorizontalParallax(r: Double): Double {
        return 8.794 / (3600.0 * r)
    }

    fun sunRightAscensionParallaxAndTopocentricDec(
        latitude: Double, elevation: Double,
        xi: Double, h: Double, δ: Double, δα: Double, δPrime: Double
    ) {
        var δα = δα
        var δPrime = δPrime
        val δαRad: Double
        val latRad = Math.toRadians(latitude)
        val xiRad = Math.toRadians(xi)
        val hRad = Math.toRadians(h)
        val δRad = Math.toRadians(δ)
        val u = MATH.atan(0.99664719 * Math.tan(latRad))
        val y = 0.99664719 * Math.sin(u) + elevation * Math.sin(latRad) / 6378140.0
        val x = Math.cos(u) + elevation * Math.cos(latRad) / 6378140.0
        δαRad = MATH.atan2(
            -x * Math.sin(xiRad) * Math.sin(hRad),
            Math.cos(δRad) - x * Math.sin(xiRad) * Math.cos(hRad)
        )
        δPrime = Math.toDegrees(
            MATH.atan2(
                (Math.sin(δRad) - y * Math.sin(xiRad)) * Math.cos(δαRad),
                Math.cos(δRad) - x * Math.sin(xiRad) * Math.cos(hRad)
            )
        )
        δα = Math.toDegrees(δαRad)
    }

    fun topocentricSunRightAscension(αDeg: Double, δα: Double): Double {
        return αDeg + δα
    }

    fun topocentricLocalHourAngle(h: Double, δα: Double): Double {
        return h - δα
    }

    fun topocentricElevationAngle(latitude: Double, δPrime: Double, hPrime: Double): Double {
        val latRad = Math.toRadians(latitude)
        val δPrimeRad = Math.toRadians(δPrime)
        return Math.toDegrees(
            MATH.asin(
                Math.sin(latRad) * Math.sin(δPrimeRad)
                        + Math.cos(latRad) * Math.cos(δPrimeRad) * Math.cos(Math.toRadians(hPrime))
            )
        )
    }

    fun atmosphericRefractionCorrection(
        pressure: Double, temperature: Double,
        atmosRefract: Double, e0: Double
    ): Double {
        var Δe = 0.0
        if (e0 >= -1 * (SUNRADIUS + atmosRefract)) {
            Δe = (pressure / 1010.0 * (283.0 / (273.0 + temperature))
                    * 1.02) / (60.0 * Math.tan(Math.toRadians(e0 + 10.3 / (e0 + 5.11))))
        }
        return Δe
    }

    fun topocentricElevationAngleCorrected(e0: Double, ΔE: Double): Double {
        return e0 + ΔE
    }

    fun topocentricZenithAngle(e: Double): Double {
        return 90.0 - e
    }

    fun topocentricAzimuthAngleNeg180180(hPrime: Double, latitude: Double, δPrime: Double): Double {
        val hPrimeRad = Math.toRadians(hPrime)
        val latRad = Math.toRadians(latitude)
        return Math.toDegrees(
            MATH.atan2(
                Math.sin(hPrimeRad),
                Math.cos(hPrimeRad) * Math.sin(latRad) - Math.tan(Math.toRadians(δPrime)) * Math.cos(
                    latRad
                )
            )
        )
    }

    fun topocentricAzimuthAngleZero360(azimuth180: Double): Double {
        return azimuth180 + 180.0
    }

    fun surfaceIncidenceAngle(
        zenith: Double, azimuth180: Double, azmRotation: Double,
        slope: Double
    ): Double {
        val zenithRad = Math.toRadians(zenith)
        val slopeRad = Math.toRadians(slope)
        return Math.toDegrees(
            MATH.acos(
                Math.cos(zenithRad) * Math.cos(slopeRad)
                        + Math.sin(slopeRad) * Math.sin(zenithRad) * Math.cos(
                    Math.toRadians(
                        azimuth180 - azmRotation
                    )
                )
            )
        )
    }

    fun approxSunTransitTime(αo: Double, longitude: Double, ν: Double): Double {
        return (αo - longitude - ν) / 360.0
    }

    fun getHourAngleAtRiseSet(latitude: Double, δo: Double, h0Prime: Double): Double {
        var h0 = -99999.0
        val latitudeRad = Math.toRadians(latitude)
        val δoRad = Math.toRadians(δo)
        val argument =
            ((Math.sin(Math.toRadians(h0Prime)) - Math.sin(latitudeRad) * Math.sin(δoRad))
                    / (Math.cos(latitudeRad) * Math.cos(δoRad)))
        if (Math.abs(argument) <= 1) {
            h0 = limitDegrees180(Math.toDegrees(MATH.acos(argument)))
        }
        return h0
    }

    fun approxSunRiseAndSet(mRts: DoubleArray, h0: Double) {
        val h0Dfrac = h0 / 360.0
        mRts[1] = limitZero2one(mRts[0] - h0Dfrac)
        mRts[2] = limitZero2one(mRts[0] + h0Dfrac)
        mRts[0] = limitZero2one(mRts[0])
    }

    fun approxSalatTimes(mRts: DoubleArray, h0: DoubleArray) {
        mRts[SUNRISE.toInt()] =
            limitZero2one(mRts[SUNTRANSIT.toInt()] - h0[SUNRISE.toInt()] / 360.0)
        mRts[SUNSET.toInt()] = limitZero2one(mRts[SUNTRANSIT.toInt()] + h0[SUNSET.toInt()] / 360.0)
        mRts[ASR_SHAFI.toInt()] =
            limitZero2one(mRts[SUNTRANSIT.toInt()] + h0[ASR_SHAFI.toInt()] / 360.0)
        mRts[ASR_HANEFI.toInt()] =
            limitZero2one(mRts[SUNTRANSIT.toInt()] + h0[ASR_HANEFI.toInt()] / 360.0)
        mRts[FAJR.toInt()] = limitZero2one(mRts[SUNTRANSIT.toInt()] - h0[FAJR.toInt()] / 360.0)
        mRts[ISHA.toInt()] = limitZero2one(mRts[SUNTRANSIT.toInt()] + h0[ISHA.toInt()] / 360.0)
        mRts[SUNTRANSIT.toInt()] = limitZero2one(mRts[SUNTRANSIT.toInt()])
    }

    fun approxKerahatTimes(mRts: DoubleArray, h0: DoubleArray) {
        mRts[FAJR_.toInt()] = limitZero2one(mRts[SUNTRANSIT_.toInt()] - h0[FAJR_.toInt()] / 360.0)
        mRts[ISRAK.toInt()] = limitZero2one(mRts[SUNTRANSIT_.toInt()] - h0[ISRAK.toInt()] / 360.0)
        mRts[ASRHANEFI.toInt()] =
            limitZero2one(mRts[SUNTRANSIT_.toInt()] + h0[ASRHANEFI.toInt()] / 360.0)
        mRts[ISFIRAR.toInt()] =
            limitZero2one(mRts[SUNTRANSIT_.toInt()] + h0[ISFIRAR.toInt()] / 360.0)
        mRts[SUNSET.toInt()] = limitZero2one(mRts[SUNTRANSIT_.toInt()] + h0[SUNSET.toInt()] / 360.0)
        mRts[SUNTRANSIT_.toInt()] = limitZero2one(mRts[SUNTRANSIT_.toInt()])
    }

    fun rtsAlphaDeltaPrime(ad: DoubleArray, n: Double): Double {
        var a = ad[1] - ad[0]
        var b = ad[2] - ad[1]
        if (Math.abs(a) >= 2.0) {
            a = limitZero2one(a)
        }
        if (Math.abs(b) >= 2.0) {
            b = limitZero2one(b)
        }
        return ad[1] + n * (a + b + (b - a) * n) / 2.0
    }

    fun Interpolate(n: Double, Y: DoubleArray): Double {
        val a = Y[1] - Y[0]
        val b = Y[2] - Y[1]
        val c = Y[0] + Y[2] - 2 * Y[1]
        return Y[1] + n / 2 * (a + b + n * c)
    }

    fun rtsSunAltitude(latitude: Double, δPrime: Double, hPrime: Double): Double {
        val latitudeRad = Math.toRadians(latitude)
        val δPrimeRad = Math.toRadians(δPrime)
        return Math.toDegrees(
            MATH.asin(
                Math.sin(latitudeRad) * Math.sin(δPrimeRad)
                        + Math.cos(latitudeRad) * Math.cos(δPrimeRad) * Math.cos(
                    Math.toRadians(
                        hPrime
                    )
                )
            )
        )
    }

    fun sunRiseAndSet(
        mRts: DoubleArray, hRts: DoubleArray, δPrime: DoubleArray, latitude: Double,
        hPrime: DoubleArray, h0Prime: Double, sun: Int
    ): Double {
        return mRts[sun] + (hRts[sun] - h0Prime)
        / 360.0 * Math.cos(Math.toRadians(δPrime[sun])) * Math.cos(Math.toRadians(latitude)) * Math.sin(
            Math.toRadians(
                hPrime[sun]
            )
        )
    }

    fun calculateSunRiseTransitSet(spa: DoubleArray, jd: Double) {
        var jd = jd
        val mRts: DoubleArray
        val hRts: DoubleArray
        val νRts: DoubleArray
        val αPrime: DoubleArray
        val δPrime: DoubleArray
        val hPrime: DoubleArray
        mRts = DoubleArray(3)
        hRts = DoubleArray(3)
        νRts = DoubleArray(3)
        αPrime = DoubleArray(3)
        δPrime = DoubleArray(3)
        hPrime = DoubleArray(3)
        val atmosRefract = 0.5667
        val h0Prime = -1 * (SUNRADIUS + atmosRefract)
        val ν: Double
        val h0: Double
        var n: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = Math.floor(jd + 0.5) - 0.5
        val ΔT = AstroLib.calculateTimeDifference(jd)
        val longitude = 32.85
        val latitude = 39.95
        val timezone = 2.0 //ANKARA position
        //double longitude =-116.8625, latitude =33.356111111111112, timezone = 0;
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT)
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT)
        ν = calculateGreenwichSiderealTime(jd, ΔT)
        mRts[0] = approxSunTransitTime(dayOfInterest.α, longitude, ν)
        h0 = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        val suntransit: Double
        val sunrise: Double
        val sunset: Double
        if (h0 >= 0) {
            approxSunRiseAndSet(mRts, h0)
            for (i in 0..2) {
                νRts[i] = ν + 360.985647 * mRts[i]
                n = mRts[i] + ΔT / 86400.0
                αPrime[i] = rtsAlphaDeltaPrime(α, n)
                δPrime[i] = rtsAlphaDeltaPrime(δ, n)
                hPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], hPrime[i])
            }
            suntransit = dayFracToLocalHour(mRts[0] - hPrime[0] / 360.0, timezone)
            sunrise = dayFracToLocalHour(
                sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 1),
                timezone
            )
            sunset = dayFracToLocalHour(
                sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 2),
                timezone
            )
            spa[0] = suntransit
            spa[1] = sunrise
            spa[2] = sunset
        }
    }

    fun calculateSunRiseTransitSet(
        jd: Double,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        ΔT: Double
    ): DoubleArray {
        var jd = jd
        val mRts: DoubleArray
        val hRts: DoubleArray
        val νRts: DoubleArray
        val αPrime: DoubleArray
        val δPrime: DoubleArray
        val hPrime: DoubleArray
        val spa: DoubleArray
        mRts = DoubleArray(3)
        hRts = DoubleArray(3)
        νRts = DoubleArray(3)
        αPrime = DoubleArray(3)
        δPrime = DoubleArray(3)
        hPrime = DoubleArray(3)
        spa = DoubleArray(3)
        val atmosRefract = 0.5667
        val h0Prime = -1 * (SUNRADIUS + atmosRefract)
        val ν: Double
        val h0: Double
        var n: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = Math.floor(jd + 0.5) - 0.5
        //double longitude = 32.85, latitude = 39.95, timezone = 2;//ANKARA position
        //double longitude =-116.8625, latitude =33.356111111111112, timezone = 0;
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT)
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT)
        ν = calculateGreenwichSiderealTime(jd, ΔT)
        mRts[0] = approxSunTransitTime(dayOfInterest.α, longitude, ν)
        h0 = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        val suntransit: Double
        val sunrise: Double
        val sunset: Double
        if (h0 >= 0) {
            approxSunRiseAndSet(mRts, h0)
            for (i in 0..2) {
                νRts[i] = ν + 360.985647 * mRts[i]
                n = mRts[i] + ΔT / 86400.0
                αPrime[i] = rtsAlphaDeltaPrime(α, n)
                δPrime[i] = rtsAlphaDeltaPrime(δ, n)
                hPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], hPrime[i])
            }
            suntransit = dayFracToLocalHour(mRts[0] - hPrime[0] / 360.0, timezone)
            sunrise = dayFracToLocalHour(
                sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 1),
                timezone
            )
            sunset = dayFracToLocalHour(
                sunRiseAndSet(mRts, hRts, δPrime, latitude, hPrime, h0Prime, 2),
                timezone
            )
            spa[0] = suntransit
            spa[1] = sunrise
            spa[2] = sunset
        }
        return spa
    }

    fun calculateSalatTimes(
        jd: Double,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        temperature: Int,
        pressure: Int,
        altitude: Int,
        fajrAngle: Double,
        ishaAngle: Double
    ): DoubleArray {
        var jd = jd
        val mRts: DoubleArray
        val hRts: DoubleArray
        val νRts: DoubleArray
        val αPrime: DoubleArray
        val δPrime: DoubleArray
        val HPrime: DoubleArray
        val salatTimes: DoubleArray
        val H0: DoubleArray
        mRts = DoubleArray(SUN_COUNT.toInt())
        hRts = DoubleArray(SUN_COUNT.toInt())
        νRts = DoubleArray(SUN_COUNT.toInt())
        H0 = DoubleArray(SUN_COUNT.toInt())
        αPrime = DoubleArray(SUN_COUNT.toInt())
        δPrime = DoubleArray(SUN_COUNT.toInt())
        salatTimes = DoubleArray(SUN_COUNT.toInt())
        HPrime =
            DoubleArray(SUN_COUNT.toInt()) //Calculate the local hour angle for the sun transit, sunrise, and sunset, H’i (in degrees),
        //double atmosRefract = 0.5667;
        val h0Prime =
            -SUNRADIUS - AstroLib.getApparentAtmosphericRefraction(0.0) * AstroLib.getWeatherCorrectionCoefficent(
                temperature,
                pressure
            ) - AstroLib.getAltitudeCorrection(altitude)
        val ν: Double
        var n: Double
        val ΔT: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = Math.floor(jd + 0.5) - 0.5
        ΔT = AstroLib.calculateTimeDifference(jd)
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT)
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT)
        //ASR ANGLE CALCULATION******************************///
        val δnoon =
            (dayOfInterest.δ + dayAfter.δ) / 2.0 //Arithmetic average for sun declination for midday
        val zenithTransit = Math.toRadians(latitude - δnoon)
        val asrShafiAngle = Math.toDegrees(MATH.atan(1 / (1 + Math.tan(zenithTransit))))
        val asrHanafiAngle = Math.toDegrees(MATH.atan(1 / (2 + Math.tan(zenithTransit))))
        //**************************************************///
        ν = calculateGreenwichSiderealTime(jd, ΔT)
        mRts[SUNTRANSIT.toInt()] = approxSunTransitTime(dayOfInterest.α, longitude, ν)
        //Calculate the local hour angle corresponding to the sun elevation equals 0.8333/,H0
        H0[SUNTRANSIT.toInt()] = 0
        H0[SUNRISE.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        H0[SUNSET.toInt()] = H0[SUNRISE.toInt()]
        H0[ASR_SHAFI.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrShafiAngle)
        H0[ASR_HANEFI.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrHanafiAngle)
        H0[FAJR.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, fajrAngle)
        H0[ISHA.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, ishaAngle)
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        approxSalatTimes(mRts, H0)
        for (i in 0 until SUN_COUNT) {
            if (H0[i] >= 0) {
                νRts[i] = ν + 360.985647 * mRts[i]
                n = mRts[i] + ΔT / 86400.0
                αPrime[i] = Interpolate(n, α)
                δPrime[i] = Interpolate(n, δ)
                HPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], HPrime[i])
            }
        }
        if (H0[SUNTRANSIT.toInt()] >= 0) {
            salatTimes[SUNTRANSIT.toInt()] = dayFracToLocalHour(
                mRts[SUNTRANSIT.toInt()] - HPrime[SUNTRANSIT.toInt()] / 360.0,
                timezone
            )
        }
        if (H0[SUNRISE.toInt()] >= 0) {
            salatTimes[SUNRISE.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    h0Prime,
                    SUNRISE.toInt()
                ), timezone
            )
        }
        if (H0[SUNSET.toInt()] >= 0) {
            salatTimes[SUNSET.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    h0Prime,
                    SUNSET.toInt()
                ), timezone
            )
        }
        if (H0[ASR_SHAFI.toInt()] >= 0) {
            salatTimes[ASR_SHAFI.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    asrShafiAngle,
                    ASR_SHAFI.toInt()
                ), timezone
            )
        }
        if (H0[ASR_HANEFI.toInt()] >= 0) {
            salatTimes[ASR_HANEFI.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    asrHanafiAngle,
                    ASR_HANEFI.toInt()
                ), timezone
            )
        }
        if (H0[FAJR.toInt()] >= 0) {
            salatTimes[FAJR.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    fajrAngle,
                    FAJR.toInt()
                ), timezone
            )
        }
        if (H0[ISHA.toInt()] >= 0) {
            salatTimes[ISHA.toInt()] = dayFracToLocalHour(
                sunRiseAndSet(
                    mRts,
                    hRts,
                    δPrime,
                    latitude,
                    HPrime,
                    ishaAngle,
                    ISHA.toInt()
                ), timezone
            )
        }
        return salatTimes
    }

    fun calculateKerahetTimes(
        jd: Double,
        latitude: Double,
        longitude: Double,
        timezone: Double,
        temperature: Int,
        pressure: Int,
        altitude: Int,
        fajrAngle: Double,
        israkIsfirarAngle: Double
    ): DoubleArray {     //final int FAJR_=0,ISRAK=1,SUNTRANSIT_=2,ASRHANEFI=3,ISFIRAR=4,SUNSET_=5,KERAHAT_COUNT=6,DUHA=7,ISTIVA=8;
        var jd = jd
        val mRts: DoubleArray
        val hRts: DoubleArray
        val νRts: DoubleArray
        val αPrime: DoubleArray
        val δPrime: DoubleArray
        val HPrime: DoubleArray
        val kerahatTimes: DoubleArray
        val H0: DoubleArray
        mRts = DoubleArray(KERAHAT_COUNT.toInt())
        hRts = DoubleArray(KERAHAT_COUNT.toInt())
        νRts = DoubleArray(KERAHAT_COUNT.toInt())
        H0 = DoubleArray(KERAHAT_COUNT.toInt())
        αPrime = DoubleArray(KERAHAT_COUNT.toInt())
        δPrime = DoubleArray(KERAHAT_COUNT.toInt())
        kerahatTimes = DoubleArray(KERAHAT_COUNT + 3)
        HPrime =
            DoubleArray(KERAHAT_COUNT.toInt()) //Calculate the local hour angle for the sun transit, sunrise, and sunset, H’i (in degrees),
        //double atmosRefract = 0.5667;
        val h0Prime =
            -SUNRADIUS - AstroLib.getApparentAtmosphericRefraction(0.0) * AstroLib.getWeatherCorrectionCoefficent(
                temperature,
                pressure
            ) - AstroLib.getAltitudeCorrection(altitude)
        val ν: Double
        var n: Double
        val ΔT: Double
        val dayBefore: Equatorial
        val dayOfInterest: Equatorial
        val dayAfter: Equatorial
        jd = Math.floor(jd + 0.5) - 0.5
        ΔT = AstroLib.calculateTimeDifference(jd)
        dayBefore = calculateSunEquatorialCoordinates(jd - 1, ΔT)
        dayOfInterest = calculateSunEquatorialCoordinates(jd, ΔT)
        dayAfter = calculateSunEquatorialCoordinates(jd + 1, ΔT)
        //ASR ANGLE CALCULATION----------------------------------///
        val δnoon =
            (dayOfInterest.δ + dayAfter.δ) / 2.0 //Arithmetic average for sun declination for midday
        val zenithTransit = Math.toRadians(latitude - δnoon)
        val asrHanafiAngle = Math.toDegrees(MATH.atan(1 / (2 + Math.tan(zenithTransit))))
        //-----------------------------------------------------///
        ν = calculateGreenwichSiderealTime(jd, ΔT)
        mRts[SUNTRANSIT_.toInt()] = approxSunTransitTime(dayOfInterest.α, longitude, ν)
        //Calculate the local hour angle corresponding to the sun elevation equals 0.8333/,H0
        H0[SUNTRANSIT_.toInt()] = 0
        H0[SUNSET.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, h0Prime)
        H0[ISRAK.toInt()] = getHourAngleAtRiseSet(
            latitude,
            dayOfInterest.δ,
            AstroLib.getApparentAtmosphericRefraction(israkIsfirarAngle)
        )
        H0[ISFIRAR.toInt()] = H0[ISRAK.toInt()]
        H0[ASRHANEFI.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, asrHanafiAngle)
        H0[FAJR_.toInt()] = getHourAngleAtRiseSet(latitude, dayOfInterest.δ, fajrAngle)
        //H0[ISHA]= getHourAngleAtRiseSet(latitude,dayOfInterest.δ,ishaAngle);
        val α = doubleArrayOf(dayBefore.α, dayOfInterest.α, dayAfter.α)
        val δ = doubleArrayOf(dayBefore.δ, dayOfInterest.δ, dayAfter.δ)
        approxKerahatTimes(mRts, H0)
        for (i in 0 until KERAHAT_COUNT) {
            if (H0[i] >= 0) {
                νRts[i] = ν + 360.985647 * mRts[i]
                n = mRts[i] + ΔT / 86400.0
                αPrime[i] = rtsAlphaDeltaPrime(α, n)
                δPrime[i] = rtsAlphaDeltaPrime(δ, n)
                HPrime[i] = limitDegrees180pm(νRts[i] + longitude - αPrime[i])
                hRts[i] = rtsSunAltitude(latitude, δPrime[i], HPrime[i])
            }
            if (H0[SUNTRANSIT_.toInt()] >= 0) {
                kerahatTimes[SUNTRANSIT_.toInt()] = dayFracToLocalHour(
                    mRts[SUNTRANSIT_.toInt()] - HPrime[SUNTRANSIT_.toInt()] / 360.0,
                    timezone
                )
            }
            if (H0[ISRAK.toInt()] >= 0) {
                kerahatTimes[ISRAK.toInt()] = dayFracToLocalHour(
                    sunRiseAndSet(
                        mRts,
                        hRts,
                        δPrime,
                        latitude,
                        HPrime,
                        israkIsfirarAngle,
                        ISRAK.toInt()
                    ), timezone
                )
            }
            if (H0[SUNSET_.toInt()] >= 0) {
                kerahatTimes[SUNSET_.toInt()] = dayFracToLocalHour(
                    sunRiseAndSet(
                        mRts,
                        hRts,
                        δPrime,
                        latitude,
                        HPrime,
                        h0Prime,
                        SUNSET_.toInt()
                    ), timezone
                )
            }
            if (H0[ASRHANEFI.toInt()] >= 0) {
                kerahatTimes[ASRHANEFI.toInt()] = dayFracToLocalHour(
                    sunRiseAndSet(
                        mRts,
                        hRts,
                        δPrime,
                        latitude,
                        HPrime,
                        asrHanafiAngle,
                        ASRHANEFI.toInt()
                    ), timezone
                )
            }
            if (H0[ISFIRAR.toInt()] >= 0) {
                kerahatTimes[ISFIRAR.toInt()] = dayFracToLocalHour(
                    sunRiseAndSet(
                        mRts,
                        hRts,
                        δPrime,
                        latitude,
                        HPrime,
                        israkIsfirarAngle,
                        ISFIRAR.toInt()
                    ), timezone
                )
            }
            if (H0[FAJR_.toInt()] >= 0) {
                kerahatTimes[FAJR_.toInt()] = dayFracToLocalHour(
                    sunRiseAndSet(
                        mRts,
                        hRts,
                        δPrime,
                        latitude,
                        HPrime,
                        fajrAngle,
                        FAJR_.toInt()
                    ), timezone
                )
            }
            kerahatTimes[DUHA.toInt()] =
                (3 * kerahatTimes[FAJR_.toInt()] + kerahatTimes[SUNSET.toInt()]) / 4
            kerahatTimes[ISTIVA.toInt()] =
                (kerahatTimes[SUNSET.toInt()] + kerahatTimes[FAJR_.toInt()]) / 2
        }
        return kerahatTimes
    }

    fun calculateSunEquatorialCoordinates(jd: Double, ΔT: Double): Equatorial {
        val jce: Double
        val jme: Double
        val jde: Double
        val Δψ: Double
        val ε: Double
        val r: Double
        val l: Double
        val β: Double
        val theta: Double
        val Δτ: Double
        val λ: Double
        val b: Double
        val Δε: Double
        val ε0: Double
        val α: Double
        val δ: Double
        val x = DoubleArray(5)
        //jc=getJulianCentury(jd);
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
        jce = AstroLib.getJulianEphemerisCentury(jde)
        jme = AstroLib.getJulianEphemerisMillennium(jce)
        // jde=getJulianEphemerisDay(jd,ΔT);
        x[0] = meanElongationMoonSun(jce)
        x[1] = meanAnomalySun(jce)
        x[2] = meanAnomalyMoon(jce)
        x[3] = argumentLatitudeMoon(jce)
        x[4] = ascendingLongitudeMoon(jce)
        ε0 = eclipticMeanObliquity(jme)
        Δε = nutationObliquity(jce, x)
        Δψ = nutationLongitude(jce, x)
        ε = eclipticTrueObliquity(Δε, ε0)
        r = earthRadiusVector(jme)
        l = earthHeliocentricLongitude(jme)
        theta = geocentricLongitude(l)
        Δτ = aberrationCorrection(r)
        λ = apparentSunLongitude(theta, Δψ, Δτ)
        // ν0= greenwichMeanSiderealTime (jd,jc);
        //ν= greenwichSiderealTime (ν0,Δψ,ε);
        b = earthHeliocentricLatitude(jme)
        β = getGeocentricLatitude(b)
        α = geocentricRightAscension(λ, ε, β)
        δ = geocentricDeclination(λ, ε, β)
        val Δ = 149597887.5
        return Equatorial(α, δ, Δ)
        //m=sunMeanLongitude(jme);
        //eot=calculateEquationOfTime(m,α,Δψ,ε);
    }

    fun calculateSunEquatorialCoordinates(sunPosEc: Ecliptic?, jd: Double, ΔT: Double): Equatorial {
        val jce: Double
        val jme: Double
        val jde: Double
        val ε: Double
        val Δε: Double
        val ε0: Double
        val α: Double
        val δ: Double
        val x = DoubleArray(5)
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
        jce = AstroLib.getJulianEphemerisCentury(jde)
        jme = AstroLib.getJulianEphemerisMillennium(jce)
        x[0] = meanElongationMoonSun(jce)
        x[1] = meanAnomalySun(jce)
        x[2] = meanAnomalyMoon(jce)
        x[3] = argumentLatitudeMoon(jce)
        x[4] = ascendingLongitudeMoon(jce)
        ε0 = eclipticMeanObliquity(jme)
        Δε = nutationObliquity(jce, x)
        ε = eclipticTrueObliquity(Δε, ε0)
        α = geocentricRightAscension(sunPosEc!!.λ, ε, sunPosEc.β)
        δ = geocentricDeclination(sunPosEc.λ, ε, sunPosEc.β)
        val Δ = 149597887.5
        return Equatorial(α, δ, Δ)
    }

    fun calculateSunEclipticCoordinatesAstronomic(jd: Double, ΔT: Double): Ecliptic {
        val jce: Double
        val jme: Double
        val jde: Double
        val l: Double
        val β: Double
        val theta: Double
        val b: Double
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
        jce = AstroLib.getJulianEphemerisCentury(jde)
        jme = AstroLib.getJulianEphemerisMillennium(jce)
        l = earthHeliocentricLongitude(jme)
        theta = geocentricLongitude(l)
        b = earthHeliocentricLatitude(jme)
        β = getGeocentricLatitude(b)
        return Ecliptic(theta, β)
    }

    fun calculateEclipticTrueObliquity(jd: Double, ΔT: Double): Double {
        val jce: Double
        val jme: Double
        val jde: Double
        val ε: Double
        val Δε: Double
        val ε0: Double
        val x = DoubleArray(5)
        jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
        jce = AstroLib.getJulianEphemerisCentury(jde)
        jme = AstroLib.getJulianEphemerisMillennium(jce)
        x[0] = meanElongationMoonSun(jce)
        x[1] = meanAnomalySun(jce)
        x[2] = meanAnomalyMoon(jce)
        x[3] = argumentLatitudeMoon(jce)
        x[4] = ascendingLongitudeMoon(jce)
        ε0 = eclipticMeanObliquity(jme) //
        Δε = nutationObliquity(jce, x) //
        ε = eclipticTrueObliquity(Δε, ε0) //
        return ε
    }

    /**
     * TA'DIL-I ZAMAN
     * Tadil-i zaman degiskenini doner.
     * Gunesin saat 12 de iken tepe noktasinda olmasi gerekirken olamayipda  aradaki farki dakika cinsinden
     * veren fonksiyon.
     * Calculate the difference between true solar time and mean. The "equation
     * of time" is a term accounting for changes in the time of solar noon for
     * a given location over the course of a year. Earth's elliptical orbit and
     * Kepler's law of equal areas in equal times are the culprits behind this
     * phenomenon. See the
     * <A HREF="http://www.analemma.com/Pages/framesPage.html">Analemma page</A>.
     * Below is a plot of the equation of time versus the day of the year.
     *
     *
     * <P align="center"><img src="doc-files/EquationOfTime.png"></img></P>
     *
     * @param t νmber of Julian centuries since J2000.
     * @return Equation of time in minutes of time.
     * TA'DIL-I ZEMAN
     */
    fun calculateEquationOfTime(M: Double, α: Double, Δψ: Double, ε: Double): Double {
        return limitMinutes(4.0 * (M - 0.0057183 - α + Δψ * Math.cos(Math.toRadians(ε))))
    }

    companion object {
        // public enum Salats {};
        ///////////////////////////////////////////////////
        ///  Earth Periodic Terms
        ///////////////////////////////////////////////////
        private val LTERMS = arrayOf(
            arrayOf(
                doubleArrayOf(175347046.0, 0.0, 0.0),
                doubleArrayOf(3341656.0, 4.6692568, 6283.07585),
                doubleArrayOf(34894.0, 4.6261, 12566.1517),
                doubleArrayOf(3497.0, 2.7441, 5753.3849),
                doubleArrayOf(3418.0, 2.8289, 3.5231),
                doubleArrayOf(3136.0, 3.6277, 77713.7715),
                doubleArrayOf(2676.0, 4.4181, 7860.4194),
                doubleArrayOf(2343.0, 6.1352, 3930.2097),
                doubleArrayOf(1324.0, 0.7425, 11506.7698),
                doubleArrayOf(1273.0, 2.0371, 529.691),
                doubleArrayOf(1199.0, 1.1096, 1577.3435),
                doubleArrayOf(990.0, 5.233, 5884.927),
                doubleArrayOf(902.0, 2.045, 26.298),
                doubleArrayOf(857.0, 3.508, 398.149),
                doubleArrayOf(780.0, 1.179, 5223.694),
                doubleArrayOf(753.0, 2.533, 5507.553),
                doubleArrayOf(505.0, 4.583, 18849.228),
                doubleArrayOf(492.0, 4.205, 775.523),
                doubleArrayOf(357.0, 2.92, 0.067),
                doubleArrayOf(317.0, 5.849, 11790.629),
                doubleArrayOf(284.0, 1.899, 796.298),
                doubleArrayOf(271.0, 0.315, 10977.079),
                doubleArrayOf(243.0, 0.345, 5486.778),
                doubleArrayOf(206.0, 4.806, 2544.314),
                doubleArrayOf(205.0, 1.869, 5573.143),
                doubleArrayOf(202.0, 2.458, 6069.777),
                doubleArrayOf(156.0, 0.833, 213.299),
                doubleArrayOf(132.0, 3.411, 2942.463),
                doubleArrayOf(126.0, 1.083, 20.775),
                doubleArrayOf(115.0, 0.645, 0.98),
                doubleArrayOf(103.0, 0.636, 4694.003),
                doubleArrayOf(102.0, 0.976, 15720.839),
                doubleArrayOf(102.0, 4.267, 7.114),
                doubleArrayOf(99.0, 6.21, 2146.17),
                doubleArrayOf(98.0, 0.68, 155.42),
                doubleArrayOf(86.0, 5.98, 161000.69),
                doubleArrayOf(85.0, 1.3, 6275.96),
                doubleArrayOf(85.0, 3.67, 71430.7),
                doubleArrayOf(80.0, 1.81, 17260.15),
                doubleArrayOf(79.0, 3.04, 12036.46),
                doubleArrayOf(75.0, 1.76, 5088.63),
                doubleArrayOf(74.0, 3.5, 3154.69),
                doubleArrayOf(74.0, 4.68, 801.82),
                doubleArrayOf(70.0, 0.83, 9437.76),
                doubleArrayOf(62.0, 3.98, 8827.39),
                doubleArrayOf(61.0, 1.82, 7084.9),
                doubleArrayOf(57.0, 2.78, 6286.6),
                doubleArrayOf(56.0, 4.39, 14143.5),
                doubleArrayOf(56.0, 3.47, 6279.55),
                doubleArrayOf(52.0, 0.19, 12139.55),
                doubleArrayOf(52.0, 1.33, 1748.02),
                doubleArrayOf(51.0, 0.28, 5856.48),
                doubleArrayOf(49.0, 0.49, 1194.45),
                doubleArrayOf(41.0, 5.37, 8429.24),
                doubleArrayOf(41.0, 2.4, 19651.05),
                doubleArrayOf(39.0, 6.17, 10447.39),
                doubleArrayOf(37.0, 6.04, 10213.29),
                doubleArrayOf(37.0, 2.57, 1059.38),
                doubleArrayOf(36.0, 1.71, 2352.87),
                doubleArrayOf(36.0, 1.78, 6812.77),
                doubleArrayOf(33.0, 0.59, 17789.85),
                doubleArrayOf(30.0, 0.44, 83996.85),
                doubleArrayOf(30.0, 2.74, 1349.87),
                doubleArrayOf(25.0, 3.16, 4690.48)
            ),
            arrayOf(
                doubleArrayOf(628331966747.0, 0.0, 0.0),
                doubleArrayOf(206059.0, 2.678235, 6283.07585),
                doubleArrayOf(4303.0, 2.6351, 12566.1517),
                doubleArrayOf(425.0, 1.59, 3.523),
                doubleArrayOf(119.0, 5.796, 26.298),
                doubleArrayOf(109.0, 2.966, 1577.344),
                doubleArrayOf(93.0, 2.59, 18849.23),
                doubleArrayOf(72.0, 1.14, 529.69),
                doubleArrayOf(68.0, 1.87, 398.15),
                doubleArrayOf(67.0, 4.41, 5507.55),
                doubleArrayOf(59.0, 2.89, 5223.69),
                doubleArrayOf(56.0, 2.17, 155.42),
                doubleArrayOf(45.0, 0.4, 796.3),
                doubleArrayOf(36.0, 0.47, 775.52),
                doubleArrayOf(29.0, 2.65, 7.11),
                doubleArrayOf(21.0, 5.34, 0.98),
                doubleArrayOf(19.0, 1.85, 5486.78),
                doubleArrayOf(19.0, 4.97, 213.3),
                doubleArrayOf(17.0, 2.99, 6275.96),
                doubleArrayOf(16.0, 0.03, 2544.31),
                doubleArrayOf(16.0, 1.43, 2146.17),
                doubleArrayOf(15.0, 1.21, 10977.08),
                doubleArrayOf(12.0, 2.83, 1748.02),
                doubleArrayOf(12.0, 3.26, 5088.63),
                doubleArrayOf(12.0, 5.27, 1194.45),
                doubleArrayOf(12.0, 2.08, 4694.0),
                doubleArrayOf(11.0, 0.77, 553.57),
                doubleArrayOf(10.0, 1.3, 6286.6),
                doubleArrayOf(10.0, 4.24, 1349.87),
                doubleArrayOf(9.0, 2.7, 242.73),
                doubleArrayOf(9.0, 5.64, 951.72),
                doubleArrayOf(8.0, 5.3, 2352.87),
                doubleArrayOf(6.0, 2.65, 9437.76),
                doubleArrayOf(6.0, 4.67, 4690.48)
            ),
            arrayOf(
                doubleArrayOf(52919.0, 0.0, 0.0),
                doubleArrayOf(8720.0, 1.0721, 6283.0758),
                doubleArrayOf(309.0, 0.867, 12566.152),
                doubleArrayOf(27.0, 0.05, 3.52),
                doubleArrayOf(16.0, 5.19, 26.3),
                doubleArrayOf(16.0, 3.68, 155.42),
                doubleArrayOf(10.0, 0.76, 18849.23),
                doubleArrayOf(9.0, 2.06, 77713.77),
                doubleArrayOf(7.0, 0.83, 775.52),
                doubleArrayOf(5.0, 4.66, 1577.34),
                doubleArrayOf(4.0, 1.03, 7.11),
                doubleArrayOf(4.0, 3.44, 5573.14),
                doubleArrayOf(3.0, 5.14, 796.3),
                doubleArrayOf(3.0, 6.05, 5507.55),
                doubleArrayOf(3.0, 1.19, 242.73),
                doubleArrayOf(3.0, 6.12, 529.69),
                doubleArrayOf(3.0, 0.31, 398.15),
                doubleArrayOf(3.0, 2.28, 553.57),
                doubleArrayOf(2.0, 4.38, 5223.69),
                doubleArrayOf(2.0, 3.75, 0.98)
            ),
            arrayOf(
                doubleArrayOf(289.0, 5.844, 6283.076),
                doubleArrayOf(35.0, 0.0, 0.0),
                doubleArrayOf(17.0, 5.49, 12566.15),
                doubleArrayOf(3.0, 5.2, 155.42),
                doubleArrayOf(1.0, 4.72, 3.52),
                doubleArrayOf(1.0, 5.3, 18849.23),
                doubleArrayOf(1.0, 5.97, 242.73)
            ),
            arrayOf(
                doubleArrayOf(114.0, 3.142, 0.0),
                doubleArrayOf(8.0, 4.13, 6283.08),
                doubleArrayOf(1.0, 3.84, 12566.15)
            ),
            arrayOf(doubleArrayOf(1.0, 3.14, 0.0))
        )
        private val BTERMS = arrayOf(
            arrayOf(
                doubleArrayOf(280.0, 3.199, 84334.662),
                doubleArrayOf(102.0, 5.422, 5507.553),
                doubleArrayOf(80.0, 3.88, 5223.69),
                doubleArrayOf(44.0, 3.7, 2352.87),
                doubleArrayOf(32.0, 4.0, 1577.34)
            ), arrayOf(doubleArrayOf(9.0, 3.9, 5507.55), doubleArrayOf(6.0, 1.73, 5223.69))
        )

        /*     private static final float BTERMS[][][] = {
    {{280.0f, 3.199f, 84334.662f}, {102.0f, 5.422f, 5507.553f}, {80, 3.88f, 5223.69f}, {44, 3.7f, 2352.87f}, {32, 4, 1577.34f}},
    {{9f, 3.9f, 5507.55f}, {6, 1.73f, 5223.69f}}
    };*/
        private val RTERMS = arrayOf(
            arrayOf(
                doubleArrayOf(100013989.0, 0.0, 0.0),
                doubleArrayOf(1670700.0, 3.0984635, 6283.07585),
                doubleArrayOf(13956.0, 3.05525, 12566.1517),
                doubleArrayOf(3084.0, 5.1985, 77713.7715),
                doubleArrayOf(1628.0, 1.1739, 5753.3849),
                doubleArrayOf(1576.0, 2.8469, 7860.4194),
                doubleArrayOf(925.0, 5.453, 11506.77),
                doubleArrayOf(542.0, 4.564, 3930.21),
                doubleArrayOf(472.0, 3.661, 5884.927),
                doubleArrayOf(346.0, 0.964, 5507.553),
                doubleArrayOf(329.0, 5.9, 5223.694),
                doubleArrayOf(307.0, 0.299, 5573.143),
                doubleArrayOf(243.0, 4.273, 11790.629),
                doubleArrayOf(212.0, 5.847, 1577.344),
                doubleArrayOf(186.0, 5.022, 10977.079),
                doubleArrayOf(175.0, 3.012, 18849.228),
                doubleArrayOf(110.0, 5.055, 5486.778),
                doubleArrayOf(98.0, 0.89, 6069.78),
                doubleArrayOf(86.0, 5.69, 15720.84),
                doubleArrayOf(86.0, 1.27, 161000.69),
                doubleArrayOf(65.0, 0.27, 17260.15),
                doubleArrayOf(63.0, 0.92, 529.69),
                doubleArrayOf(57.0, 2.01, 83996.85),
                doubleArrayOf(56.0, 5.24, 71430.7),
                doubleArrayOf(49.0, 3.25, 2544.31),
                doubleArrayOf(47.0, 2.58, 775.52),
                doubleArrayOf(45.0, 5.54, 9437.76),
                doubleArrayOf(43.0, 6.01, 6275.96),
                doubleArrayOf(39.0, 5.36, 4694.0),
                doubleArrayOf(38.0, 2.39, 8827.39),
                doubleArrayOf(37.0, 0.83, 19651.05),
                doubleArrayOf(37.0, 4.9, 12139.55),
                doubleArrayOf(36.0, 1.67, 12036.46),
                doubleArrayOf(35.0, 1.84, 2942.46),
                doubleArrayOf(33.0, 0.24, 7084.9),
                doubleArrayOf(32.0, 0.18, 5088.63),
                doubleArrayOf(32.0, 1.78, 398.15),
                doubleArrayOf(28.0, 1.21, 6286.6),
                doubleArrayOf(28.0, 1.9, 6279.55),
                doubleArrayOf(26.0, 4.59, 10447.39)
            ),
            arrayOf(
                doubleArrayOf(103019.0, 1.10749, 6283.07585),
                doubleArrayOf(1721.0, 1.0644, 12566.1517),
                doubleArrayOf(702.0, 3.142, 0.0),
                doubleArrayOf(32.0, 1.02, 18849.23),
                doubleArrayOf(31.0, 2.84, 5507.55),
                doubleArrayOf(25.0, 1.32, 5223.69),
                doubleArrayOf(18.0, 1.42, 1577.34),
                doubleArrayOf(10.0, 5.91, 10977.08),
                doubleArrayOf(9.0, 1.42, 6275.96),
                doubleArrayOf(9.0, 0.27, 5486.78)
            ),
            arrayOf(
                doubleArrayOf(4359.0, 5.7846, 6283.0758),
                doubleArrayOf(124.0, 5.579, 12566.152),
                doubleArrayOf(12.0, 3.14, 0.0),
                doubleArrayOf(9.0, 3.63, 77713.77),
                doubleArrayOf(6.0, 1.87, 5573.14),
                doubleArrayOf(3.0, 5.47, 18849.23)
            ),
            arrayOf(doubleArrayOf(145.0, 4.273, 6283.076), doubleArrayOf(7.0, 3.92, 12566.15)),
            arrayOf(doubleArrayOf(4.0, 2.56, 6283.08))
        )

        ////////////////////////////////////////////////////////////////
        ///  Periodic Terms for the nutation in longitude and obliquity
        ////////////////////////////////////////////////////////////////
        private val YTERMS = arrayOf(
            byteArrayOf(0, 0, 0, 0, 1),
            byteArrayOf(-2, 0, 0, 2, 2),
            byteArrayOf(0, 0, 0, 2, 2),
            byteArrayOf(0, 0, 0, 0, 2),
            byteArrayOf(0, 1, 0, 0, 0),
            byteArrayOf(0, 0, 1, 0, 0),
            byteArrayOf(-2, 1, 0, 2, 2),
            byteArrayOf(0, 0, 0, 2, 1),
            byteArrayOf(0, 0, 1, 2, 2),
            byteArrayOf(-2, -1, 0, 2, 2),
            byteArrayOf(-2, 0, 1, 0, 0),
            byteArrayOf(-2, 0, 0, 2, 1),
            byteArrayOf(0, 0, -1, 2, 2),
            byteArrayOf(2, 0, 0, 0, 0),
            byteArrayOf(0, 0, 1, 0, 1),
            byteArrayOf(2, 0, -1, 2, 2),
            byteArrayOf(0, 0, -1, 0, 1),
            byteArrayOf(0, 0, 1, 2, 1),
            byteArrayOf(-2, 0, 2, 0, 0),
            byteArrayOf(0, 0, -2, 2, 1),
            byteArrayOf(2, 0, 0, 2, 2),
            byteArrayOf(0, 0, 2, 2, 2),
            byteArrayOf(0, 0, 2, 0, 0),
            byteArrayOf(-2, 0, 1, 2, 2),
            byteArrayOf(0, 0, 0, 2, 0),
            byteArrayOf(-2, 0, 0, 2, 0),
            byteArrayOf(0, 0, -1, 2, 1),
            byteArrayOf(0, 2, 0, 0, 0),
            byteArrayOf(2, 0, -1, 0, 1),
            byteArrayOf(-2, 2, 0, 2, 2),
            byteArrayOf(0, 1, 0, 0, 1),
            byteArrayOf(-2, 0, 1, 0, 1),
            byteArrayOf(0, -1, 0, 0, 1),
            byteArrayOf(0, 0, 2, -2, 0),
            byteArrayOf(2, 0, -1, 2, 1),
            byteArrayOf(2, 0, 1, 2, 2),
            byteArrayOf(0, 1, 0, 2, 2),
            byteArrayOf(-2, 1, 1, 0, 0),
            byteArrayOf(0, -1, 0, 2, 2),
            byteArrayOf(2, 0, 0, 2, 1),
            byteArrayOf(2, 0, 1, 0, 0),
            byteArrayOf(-2, 0, 2, 2, 2),
            byteArrayOf(-2, 0, 1, 2, 1),
            byteArrayOf(2, 0, -2, 0, 1),
            byteArrayOf(2, 0, 0, 0, 1),
            byteArrayOf(0, -1, 1, 0, 0),
            byteArrayOf(-2, -1, 0, 2, 1),
            byteArrayOf(-2, 0, 0, 0, 1),
            byteArrayOf(0, 0, 2, 2, 1),
            byteArrayOf(-2, 0, 2, 0, 1),
            byteArrayOf(-2, 1, 0, 2, 1),
            byteArrayOf(0, 0, 1, -2, 0),
            byteArrayOf(-1, 0, 1, 0, 0),
            byteArrayOf(-2, 1, 0, 0, 0),
            byteArrayOf(1, 0, 0, 0, 0),
            byteArrayOf(0, 0, 1, 2, 0),
            byteArrayOf(0, 0, -2, 2, 2),
            byteArrayOf(-1, -1, 1, 0, 0),
            byteArrayOf(0, 1, 1, 0, 0),
            byteArrayOf(0, -1, 1, 2, 2),
            byteArrayOf(2, -1, -1, 2, 2),
            byteArrayOf(0, 0, 3, 2, 2),
            byteArrayOf(2, -1, 0, 2, 2)
        )
        private val PETERMS = arrayOf(
            doubleArrayOf(-171996.0, -174.2, 92025.0, 8.9),
            doubleArrayOf(-13187.0, -1.6, 5736.0, -3.1),
            doubleArrayOf(-2274.0, -0.2, 977.0, -0.5),
            doubleArrayOf(2062.0, 0.2, -895.0, 0.5),
            doubleArrayOf(1426.0, -3.4, 54.0, -0.1),
            doubleArrayOf(712.0, 0.1, -7.0, 0.0),
            doubleArrayOf(-517.0, 1.2, 224.0, -0.6),
            doubleArrayOf(-386.0, -0.4, 200.0, 0.0),
            doubleArrayOf(-301.0, 0.0, 129.0, -0.1),
            doubleArrayOf(217.0, -0.5, -95.0, 0.3),
            doubleArrayOf(-158.0, 0.0, 0.0, 0.0),
            doubleArrayOf(129.0, 0.1, -70.0, 0.0),
            doubleArrayOf(123.0, 0.0, -53.0, 0.0),
            doubleArrayOf(63.0, 0.0, 0.0, 0.0),
            doubleArrayOf(63.0, 0.1, -33.0, 0.0),
            doubleArrayOf(-59.0, 0.0, 26.0, 0.0),
            doubleArrayOf(-58.0, -0.1, 32.0, 0.0),
            doubleArrayOf(-51.0, 0.0, 27.0, 0.0),
            doubleArrayOf(48.0, 0.0, 0.0, 0.0),
            doubleArrayOf(46.0, 0.0, -24.0, 0.0),
            doubleArrayOf(-38.0, 0.0, 16.0, 0.0),
            doubleArrayOf(-31.0, 0.0, 13.0, 0.0),
            doubleArrayOf(29.0, 0.0, 0.0, 0.0),
            doubleArrayOf(29.0, 0.0, -12.0, 0.0),
            doubleArrayOf(26.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-22.0, 0.0, 0.0, 0.0),
            doubleArrayOf(21.0, 0.0, -10.0, 0.0),
            doubleArrayOf(17.0, -0.1, 0.0, 0.0),
            doubleArrayOf(16.0, 0.0, -8.0, 0.0),
            doubleArrayOf(-16.0, 0.1, 7.0, 0.0),
            doubleArrayOf(-15.0, 0.0, 9.0, 0.0),
            doubleArrayOf(-13.0, 0.0, 7.0, 0.0),
            doubleArrayOf(-12.0, 0.0, 6.0, 0.0),
            doubleArrayOf(11.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-10.0, 0.0, 5.0, 0.0),
            doubleArrayOf(-8.0, 0.0, 3.0, 0.0),
            doubleArrayOf(7.0, 0.0, -3.0, 0.0),
            doubleArrayOf(-7.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-7.0, 0.0, 3.0, 0.0),
            doubleArrayOf(-7.0, 0.0, 3.0, 0.0),
            doubleArrayOf(6.0, 0.0, 0.0, 0.0),
            doubleArrayOf(6.0, 0.0, -3.0, 0.0),
            doubleArrayOf(6.0, 0.0, -3.0, 0.0),
            doubleArrayOf(-6.0, 0.0, 3.0, 0.0),
            doubleArrayOf(-6.0, 0.0, 3.0, 0.0),
            doubleArrayOf(5.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-5.0, 0.0, 3.0, 0.0),
            doubleArrayOf(-5.0, 0.0, 3.0, 0.0),
            doubleArrayOf(-5.0, 0.0, 3.0, 0.0),
            doubleArrayOf(4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-3.0, 0.0, 0.0, 0.0)
        )
        private var L_COUNT: Byte = 0
        private var B_COUNT: Byte = 0
        private var R_COUNT: Byte = 0
        private var Y_COUNT: Byte = 0
        fun limitDegrees(degrees: Double): Double {
            var degrees = degrees
            var limited: Double
            degrees /= 360.0
            limited = 360.0 * (degrees - Math.floor(degrees))
            if (limited < 0) {
                limited += 360.0
            }
            return limited
        }

        /**
         * Calculate the mean elongation of the moon from the sun, X0 (in degrees),
         *  X0 = 297.85036 + 445267.111480 * JCE- 0.0019142*JCE^2 +JCE^3/189474
         *
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return mean elongation of the moon from the sun, X0 (in degrees).
         */
        private fun meanElongationMoonSun(jce: Double): Double {
            return AstroLib.thirdOrderPolynomial(
                1.0 / 189474.0,
                -0.0019142,
                445267.11148,
                297.85036,
                jce
            )
        }

        /**
         * Calculate the mean anomaly of the sun (Earth), X1 (in degrees),
         * X1 = 357.52772 + 35999.050340 * JCE−0.0001603*JCE^2 +JCE^3/300000
         *
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return mean anomaly of the sun (Earth), X1 (in degrees).
         */
        private fun meanAnomalySun(jce: Double): Double {
            return AstroLib.thirdOrderPolynomial(
                -1.0 / 300000.0,
                -0.0001603,
                35999.05034,
                357.52772,
                jce
            )
        }

        /**
         * Calculate the mean anomaly of the moon, X2 (in degrees),
         * X2 = 134.96298 + 477198.867398 * JCE + 0.0086972 *JCE^2 +JCE^3/56250
         *
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return mean anomaly of the moon, X2 (in degrees).
         */
        private fun meanAnomalyMoon(jce: Double): Double {
            return AstroLib.thirdOrderPolynomial(
                1.0 / 56250.0,
                0.0086972,
                477198.867398,
                134.96298,
                jce
            )
        }

        /**
         * Calculate the moon’s argument of latitude, X3 (in degrees),
         *  X3 = 9327191 + 483202.017538 * JCE −0.0036825 * JCE^2+JCE^3/327270
         *
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return the moon’s argument of latitude, X3 (in degrees).
         */
        private fun argumentLatitudeMoon(jce: Double): Double {
            return AstroLib.thirdOrderPolynomial(
                1.0 / 327270.0,
                -0.0036825,
                483202.017538,
                93.27191,
                jce
            )
        }

        /**
         * Calculate the longitude of the ascending node of the moon’s mean orbit on the ecliptic,
         * measured from the mean equinox of the date, X4 (in degrees),
         *  X4 = 125.04452 − 1934.136261 * JCE+0.0020708*JCE^2 +JCE^3/450000
         *
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return mean elongation of the moon from the sun, X0 (in degrees).
         */
        private fun ascendingLongitudeMoon(jce: Double): Double {
            return AstroLib.thirdOrderPolynomial(
                1.0 / 450000.0,
                0.0020708,
                -1934.136261,
                125.04452,
                jce
            )
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        /// Calculate the Earth heliocentric longitude, latitude, and radius vector (L, B, and R): BEGIN///
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Calculate ΣXj*Yij Term Summation Xj is the jth X calculated by
         *
         *  using  meanElongationMoonSun through ascendingLongitudeMoon fuctions
         *
         *  Yi, j is the value listed in ith row and jth Y column YTERMS matrix
         *
         * @param i νmber
         * @param x xj
         * @return ΣXj*Yij  Term Summation
         */
        private fun xyTermSummation(i: Int, x: DoubleArray): Double {
            var j: Int
            var sum = 0.0
            val TERM_Y_COUNT = x.size
            j = 0
            while (j < TERM_Y_COUNT) {
                sum += x[j] * YTERMS[i][j]
                j++
            }
            return sum
        }

        /**
         * Calculate the nutation in obliquity,Δε  in degrees
         *  Δε= ΣΔεi(i=0..n) / 36000000
         *  Δεi = (c + d * JCE )*cos ( Σ X *Y )
         *
         * @param Δεi the mean obliquity of the ecliptic  Δεi (in arc seconds)
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return the nutation in obliquity,Δε  in degrees
         */
        fun nutationObliquity(jce: Double, Δεi: DoubleArray): Double {
            var i: Int
            var xyTermSum: Double
            var sumε = 0.0
            Y_COUNT = YTERMS.size.toByte()
            i = 0
            while (i < Y_COUNT) {
                xyTermSum = Math.toRadians(xyTermSummation(i, Δεi))
                sumε += (PETERMS[i][2] + jce * PETERMS[i][3]) * Math.cos(xyTermSum)
                i++
            }
            return sumε / 36000000.0
        }

        /**
         * Calculate the nutation in longitude, Δψ (in degrees),
         * Δψ = ΣΔψi(i=0..n)/36000000
         * Δψi = (ai + bi * JCE )*sin ( Σ Xj *Yij )
         *
         * @param Δψi the mean obliquity of the ecliptic  Δψi  (in arc seconds)
         * @param jce the Julian Ephemeris Century (JCE) for the 2000 standard epoch.
         * @return the nutation in longitude,  Δψ  (in degrees),
         */
        fun nutationLongitude(jce: Double, X: DoubleArray): Double {
            var i: Int
            var xyTermSum: Double
            var sumPsi = 0.0
            Y_COUNT = YTERMS.size.toByte()
            i = 0
            while (i < Y_COUNT) {
                xyTermSum = Math.toRadians(xyTermSummation(i, X))
                sumPsi += (PETERMS[i][0] + jce * PETERMS[i][1]) * Math.sin(xyTermSum)
                i++
            }
            return sumPsi / 36000000.0
        }

        /**
         * Calculate the true obliquity of the ecliptic, ε(in degrees),
         * ε=ε0+Δε/3600
         *
         * @param ε0 the nutation in obliquity Δε in degrees.
         * @param Δε the mean obliquity of the ecliptic  ε0 (in arc seconds)
         * @return the true obliquity of the ecliptic, ε(in degrees),
         */
        fun eclipticTrueObliquity(Δε: Double, ε0: Double): Double {
            return Δε + ε0 / 3600.0
        }

        /**
         * Calculate the mean obliquity of the ecliptic, ε0 (in arc seconds)
         * ε0 = 84381.448−4680.93U −155 U^2 + 1999.25U^3
         * ...−51.38U^4− -249.67U^5 − 39.05U^6 + 7.12 U^7 + 27.87 U^8 + 5.79U^9 + 2.45U^10
         * where  U = JME/10.
         *
         * @param jme the Julian Ephemeris Millennium (JME) for the 2000 standard epoch.
         * @return the mean obliquity of the ecliptic  ε0 (in arc seconds)
         */
        fun eclipticMeanObliquity(jme: Double): Double {
            val u = jme / 10.0
            return 84381.448 + u * (-4680.93 + u * (-1.55 + u * (1999.25 + u * (-51.38 + u * (-249.67
                    + u * (-39.05 + u * (7.12 + u * (27.87 + u * (5.79 + u * 2.45)))))))))
        }

        /**
         * Calculate the mean sidereal time at Greenwich, ν0 (in degrees),:
         *  ν0=280.46061837+ 360.98564736629 *(JD −2451545)+0.000387933*JC^2−JC^3/38710000
         *
         * @param jd is the Julian Day
         * @return the mean  sidereal time at Greenwich, ν0 (in degrees)
         */
        fun greenwichMeanSiderealTime(jd: Double): Double {
            val jc = (jd - 2451545.0) / 36525.0 // jc the Julian  Century
            return limitDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) + jc * jc * (0.000387933 - jc / 38710000.0))
        }

        /**
         * Calculate the apparent sidereal time at Greenwich, ν (in degrees):
         *  ν=ν+Δψcos(ε)
         *
         * @param ε JME is the Julian Ephemeris Millennium.
         * @return the apparent sidereal time at Greenwich, ν (in degrees)
         */
        fun greenwichSiderealTime(ν0: Double, Δψ: Double, ε: Double): Double {
            return ν0 + Δψ * Math.cos(Math.toRadians(ε))
        }

        /**
         * Calculate the geocentric sun right ascension, α (in degrees):
         *  α=  arctan( (sin λ*cos ε−tan β*sin ε)/cos λ )
         *
         * @param λ apparent sun longitude (in degrees)
         * @param ε the true obliquity of the ecliptic (in degrees)
         * @param β the geocentric latitude (in degrees)
         * @return α is the geocentric right ascention (in degrees).
         */
        fun geocentricRightAscension(λ: Double, ε: Double, β: Double): Double {
            val λRad = Math.toRadians(λ)
            val εRad = Math.toRadians(ε)
            return limitDegrees(
                Math.toDegrees(
                    MATH.atan2(
                        Math.sin(λRad) * Math.cos(εRad) - Math.tan(
                            Math.toRadians(β) * Math.sin(εRad)
                        ), Math.cos(λRad)
                    )
                )
            )
        }

        /**
         * Calculate the geocentric sun declination δ (in degrees):
         *      δ= asin(sin β*cos ε+cos β*sin ε*sin λ)
         *
         * @param λ apparent sun longitude (in degrees)
         * @param ε the true obliquity of the ecliptic (in degrees)
         * @param β the geocentric latitude (in degrees)
         * @return the geocentric sun declination, δ (in degrees):
         */
        fun geocentricDeclination(λ: Double, ε: Double, β: Double): Double {
            val βRad = Math.toRadians(β)
            val εRad = Math.toRadians(ε)
            return Math.toDegrees(
                MATH.asin(
                    Math.sin(βRad) * Math.cos(εRad)
                            + Math.cos(βRad) * Math.sin(εRad) * Math.sin(Math.toRadians(λ))
                )
            )
        }

        ///////////////////////////////////////////////
        fun calculateGreenwichSiderealTime(jd: Double, ΔT: Double): Double {
            val jce: Double
            val jme: Double
            val jde: Double
            val Δψ: Double
            val ε: Double
            val ν0: Double
            val Δε: Double
            val ε0: Double
            val ν: Double
            val x = DoubleArray(5)
            jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
            jce = AstroLib.getJulianEphemerisCentury(jde)
            jme = AstroLib.getJulianEphemerisMillennium(jce)
            x[0] = meanElongationMoonSun(jce)
            x[1] = meanAnomalySun(jce)
            x[2] = meanAnomalyMoon(jce)
            x[3] = argumentLatitudeMoon(jce)
            x[4] = ascendingLongitudeMoon(jce)
            ε0 = eclipticMeanObliquity(jme) //
            Δε = nutationObliquity(jce, x) //
            Δψ = nutationLongitude(jce, x) //
            ε = eclipticTrueObliquity(Δε, ε0) //
            ν0 = greenwichMeanSiderealTime(jd)
            ν = greenwichSiderealTime(ν0, Δψ, ε)
            return ν
        }

        /*   void calculateNutationAndObliquity(double jd,double ΔT, double[] Δψ, double[] ε)
    {
    double jc,jce,jme,jde,Δε,ε0;
    double ν;
    double[] x=new double[5];
    //jc=AstroLib.getJulianCentury(jd);
    jde=AstroLib.getJulianEphemerisDay(jd,ΔT);
    jce=AstroLib.getJulianEphemerisCentury(jde);
    jme=AstroLib.getJulianEphemerisMillennium(jce);
    x[0] = meanElongationMoonSun(jce);
    x[1] = meanAnomalySun(jce);
    x[2] = meanAnomalyMoon(jce);
    x[3] = argumentLatitudeMoon(jce);
    x[4] = ascendingLongitudeMoon(jce);
    ε0=eclipticMeanObliquity(jme);//
    Δε=nutationObliquity(jce, x);//
    Δψ[0]=nutationLongitude(jce,x);//
    ε[0]=eclipticTrueObliquity(Δε, ε0);//

    }*/
        fun calculateXArray(jd: Double, ΔT: Double): DoubleArray {
            val jce: Double
            val jde: Double
            val x = DoubleArray(5)
            jde = AstroLib.getJulianEphemerisDay(jd, ΔT)
            jce = AstroLib.getJulianEphemerisCentury(jde)
            x[0] = meanElongationMoonSun(jce)
            x[1] = meanAnomalySun(jce)
            x[2] = meanAnomalyMoon(jce)
            x[3] = argumentLatitudeMoon(jce)
            x[4] = ascendingLongitudeMoon(jce)
            return x
        }
    }
}
