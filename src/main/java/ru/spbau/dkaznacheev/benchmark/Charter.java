package ru.spbau.dkaznacheev.benchmark;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Charter {
    public static void makeChart(
            ServerProtocols.ServerParametersMsg params,
            List<Integer> dataSeries,
            String yLabel) {
        XYSeries series = new XYSeries(params.getArchitecture());
        int pMin = params.getPMin();
        int pStep = params.getPStep();
        int i = 0;
        for (int yValue: dataSeries) {
            series.add(pMin + i * pStep, yValue);
            i++;
        }
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);
        String xLabel = params.getParameter();
        JFreeChart chart = ChartFactory.createXYLineChart(
                yLabel + "(" + xLabel + ") on " + params.getArchitecture() + " server",
                xLabel,
                yLabel,
                collection,
                PlotOrientation.VERTICAL,
                false, true, false
                );
        File chartFile = new File(yLabel + "(" + xLabel + ")_" + params.getArchitecture() + ".jpeg");
        try {
            ChartUtilities.saveChartAsJPEG(chartFile, chart, 640, 480);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
