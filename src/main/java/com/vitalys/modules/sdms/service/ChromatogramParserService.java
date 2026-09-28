package com.vitalys.modules.sdms.service;

import com.vitalys.modules.sdms.dto.PeakResponse;
import com.vitalys.modules.sdms.entity.ChromatogramPeak;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance Parser and Synthetic Curve Generator for Chromatography (HPLC/GC)
 * and Spectroscopy (UV-Vis/FTIR) data files.
 */
@Slf4j
@Service
public class ChromatogramParserService {

    /**
     * Parses a CSV/TSV input stream containing (Time, Intensity) or (Wavelength, Absorbance).
     */
    public List<List<Double>> parseDataPoints(InputStream inputStream) {
        List<List<Double>> points = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("Time") || line.startsWith("Wavelength")) {
                    continue;
                }
                String[] parts = line.split("[,;\\t]");
                if (parts.length >= 2) {
                    try {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        points.add(List.of(x, y));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing raw data stream, falling back", e);
        }
        return points;
    }

    /**
     * Generates a high-precision, realistic HPLC Chromatogram (500 data points, 0 - 8 minutes).
     * Simulates baseline drift, detector noise, solvent front, and Gaussian chromatographic peaks.
     */
    public List<List<Double>> generateHplcChromatogram(List<ChromatogramPeak> peaks) {
        List<List<Double>> points = new ArrayList<>();
        double tStart = 0.0;
        double tEnd = 8.0;
        double dt = 0.01; // 10ms sampling interval (~800 points)

        for (double t = tStart; t <= tEnd; t += dt) {
            // Baseline noise (~0.2 mAU)
            double noise = (Math.sin(t * 12.0) * 0.08) + (Math.cos(t * 31.0) * 0.05);
            // Gentle baseline slope
            double baseline = 1.2 + (0.15 * t) + noise;

            // Solvent front at t ~ 0.65 min
            double solventPeak = 12.0 * Math.exp(-Math.pow(t - 0.65, 2) / (2 * Math.pow(0.08, 2)));

            double intensity = baseline + solventPeak;

            // Add configured chromatographic peaks (Gaussian profile: I = H * exp(-(t-tR)^2 / (2*w^2)))
            if (peaks != null) {
                for (ChromatogramPeak p : peaks) {
                    double tR = p.getRetentionTime();
                    double height = p.getPeakHeight() != null && p.getPeakHeight() > 0 ?
                            p.getPeakHeight() / 1000.0 : 50.0; // scale to mAU
                    double width = 0.07; // standard peak width in minutes

                    double peakSignal = height * Math.exp(-Math.pow(t - tR, 2) / (2 * Math.pow(width, 2)));
                    intensity += peakSignal;
                }
            } else {
                // Default Paracetamol Assay Peaks:
                // Peak 1: Impurity 4-Aminophenol at tR = 1.85 min
                double p1 = 2.5 * Math.exp(-Math.pow(t - 1.85, 2) / (2 * Math.pow(0.05, 2)));
                // Peak 2: Paracetamol Active Ingredient at tR = 3.42 min
                double p2 = 98.4 * Math.exp(-Math.pow(t - 3.42, 2) / (2 * Math.pow(0.09, 2)));
                intensity += (p1 + p2);
            }

            points.add(List.of(Math.round(t * 100.0) / 100.0, Math.round(intensity * 1000.0) / 1000.0));
        }

        return points;
    }

    /**
     * Generates a high-precision UV-Vis Spectrum (200 nm to 400 nm).
     * Simulates Paracetamol characteristic absorption band in 0.1M NaOH (Lambda max = 257 nm, A = 0.715).
     */
    public List<List<Double>> generateUvVisSpectrum() {
        List<List<Double>> points = new ArrayList<>();
        double startWl = 200.0;
        double endWl = 400.0;
        double dWl = 0.5; // 0.5 nm resolution (400 points)

        for (double wl = startWl; wl <= endWl; wl += dWl) {
            // Cutoff absorption below 215 nm
            double lowUvCutoff = 1.8 * Math.exp(-Math.pow(wl - 205.0, 2) / 60.0);

            // Primary chromophore band at 257.0 nm (A_max = 0.715)
            double mainBand = 0.715 * Math.exp(-Math.pow(wl - 257.0, 2) / (2 * Math.pow(18.5, 2)));

            // Secondary shoulder at 295 nm (weak)
            double shoulder = 0.08 * Math.exp(-Math.pow(wl - 295.0, 2) / (2 * Math.pow(25.0, 2)));

            // Baseline noise (~0.002 AU)
            double noise = (Math.sin(wl * 1.5) * 0.0015);
            double absorbance = Math.max(0.001, 0.012 + lowUvCutoff + mainBand + shoulder + noise);

            points.add(List.of(Math.round(wl * 10.0) / 10.0, Math.round(absorbance * 10000.0) / 10000.0));
        }

        return points;
    }
}
