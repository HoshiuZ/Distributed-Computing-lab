import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.File;
import java.util.Deque;

public class LogVisualDraw {
    private static final int T = 5;

    public static void draw(String deviceId, Deque<AnalysisResult> results) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int size = results.size();
        for(AnalysisResult result : results) {
            dataset.addValue(result.errorRatio * 100, "ERROR Ratio", String.valueOf(-T * size));
            dataset.addValue(result.warnRatio * 100, "WARN Ratio", String.valueOf(-T * size));
            size--;
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "LogVisualization of device " + deviceId,
                "Time(s)",
                "Ratio(%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        File dir = new File("pic");
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String filePath = "pic/" + deviceId + "_" + timestamp + ".png";
        File outputFile = new File(filePath);

        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        System.out.println("The chart is saved to :" + outputFile.getAbsolutePath());
    }
}
