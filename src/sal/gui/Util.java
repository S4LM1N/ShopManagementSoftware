package sal.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import sal.gui.core.Prodotto;

/**
 *
 * @author Salvatore Minasola
 */
public class Util {
    
    private static XYDataset createDatasetDailyChart(DefaultTableModel vendite,String dataInserita){
        
        LocalDate dataScelta = LocalDate.parse(dataInserita, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        
        TimeSeries s1 = new TimeSeries("Ricavo dai prodotti venduti su base oraria");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        
        Map<LocalDateTime, Float> filteredDates = new HashMap<>(); 
        
        for(int i=0;i<vendite.getRowCount();i++){
            
            String rowDate = vendite.getValueAt(i, 3).toString();
            LocalDateTime data = LocalDateTime.parse(rowDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            if(data.toLocalDate().equals(dataScelta))
            {
                float rowTot = (float) vendite.getValueAt(i, 1);
                filteredDates.put(data, filteredDates.getOrDefault(data, 0f) + rowTot);
            }
        }
        
        Map<Integer, Float> hourlyTotals = new HashMap<>();
        for(Map.Entry<LocalDateTime,Float> entry : filteredDates.entrySet()){
            LocalDateTime data = entry.getKey();
            float rowTot = entry.getValue();
            int hour = data.getHour();

            hourlyTotals.put(hour, hourlyTotals.getOrDefault(hour, 0f) + rowTot);
        }
        
        for (Map.Entry<Integer, Float> entry : hourlyTotals.entrySet()) {
            int hour = entry.getKey();
            float total = entry.getValue();
            s1.add(new Hour(hour, dataScelta.getDayOfMonth(), dataScelta.getMonthValue(), dataScelta.getYear()), total);
        }
         
            
        dataset.addSeries(s1);
        
        return dataset;
    }
    
    private static DefaultCategoryDataset createDatasetWeeklyChart(DefaultTableModel vendite,String settScelta){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate dataInizio = LocalDate.parse(settScelta, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        dataInizio.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALY);
        LocalDate dataCorrente = dataInizio;
        String convertedDate;

        
        
        for(int i=0;i<7;i++){
            convertedDate = String.format("%02d", dataCorrente.getDayOfMonth())+"/" +String.format("%02d", dataCorrente.getMonthValue())+"/"+dataCorrente.getYear();
            dataset.addValue(getTotGiorno(vendite,convertedDate), "Giorno",dataCorrente.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ITALY));
            dataCorrente = dataCorrente.plusDays(1);
        }
        
        return dataset;
    }
    
    private static JFreeChart createChartDaily(XYDataset dataset,String dataInserita,float tot) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            dataInserita + " totale vendite: " + tot+"\u20ac",  // title
            "Ora",             // x-axis label
            "Guadagni",   // y-axis label
            dataset);

        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        
       

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
            
            renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator()); // Assuming the first series, adjust as needed
            renderer.setSeriesItemLabelsVisible(0, true); // Assuming the first series, adjust as needed
            
        }

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        
        DecimalFormat decimalFormat = new DecimalFormat("#####");
        yAxis.setNumberFormatOverride(decimalFormat);
        
        axis.setDateFormatOverride(new SimpleDateFormat("HH"));
        
        

        return chart;

    }
    
    private static JFreeChart createChartWeekly(CategoryDataset dataset,String dataInserita){
        JFreeChart chart = ChartFactory.createBarChart(
                "Guadagni dal " + dataInserita +" al " + LocalDate.parse(dataInserita, DateTimeFormatter.ofPattern("dd/MM/yyyy")).plusDays(6),
                "Giorno della settimana",
                "Valore",
                dataset);

        
        CategoryPlot plot = chart.getCategoryPlot();
        CustomBarRenderer renderer = new CustomBarRenderer();
        plot.setRenderer(renderer);

        renderer.setBarPainter(new StandardBarPainter());
        
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        
        renderer.setDefaultItemLabelsVisible(true);

        
        ItemLabelPosition position = new ItemLabelPosition(
                ItemLabelAnchor.INSIDE12, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, -Math.PI / 2);
        renderer.setDefaultPositiveItemLabelPosition(position);

        
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 20));
        renderer.setDefaultItemLabelPaint(Color.BLACK);
        renderer.setGradientPaintTransformer(null);
        
        return chart;
    }
    
    public static ChartPanel createPanelChartDaily(DefaultTableModel vendite,String dataInserita) {
        JFreeChart chart = createChartDaily(createDatasetDailyChart(vendite,dataInserita),dataInserita,getTotGiorno(vendite, dataInserita));
        ChartPanel panelChart = new ChartPanel(chart, false);
        panelChart.setFillZoomRectangle(false);
        panelChart.setMouseWheelEnabled(true);
        panelChart.setMouseZoomable(false);
        return panelChart;
    }
    
    public static ChartPanel createPanelChartWeekly(DefaultTableModel vendite,String dataInserita){
        JFreeChart chart = createChartWeekly(createDatasetWeeklyChart(vendite,dataInserita),dataInserita);
        ChartPanel panelChart = new ChartPanel(chart, false);
        panelChart.setFillZoomRectangle(false);
        panelChart.setMouseWheelEnabled(false);
        panelChart.setMouseZoomable(false);
        return panelChart;
    }
    
    public static void updatePanelChartDaily(ChartPanel chartPanel, DefaultTableModel vendite,String dataInserita) {
        JFreeChart chart = createChartDaily(createDatasetDailyChart(vendite,dataInserita),dataInserita,getTotGiorno(vendite, dataInserita));
        chartPanel.setChart(chart);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    public static void updatePanelChartWeekly(ChartPanel chartPanel, DefaultTableModel vendite,String dataInserita) {
        JFreeChart chart = createChartWeekly(createDatasetWeeklyChart(vendite,dataInserita),dataInserita);
        chartPanel.setChart(chart);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    public static ArrayList<String> getLast7Days() {
        ArrayList<String> last7DaysFormatted = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 7; i++) {
            last7DaysFormatted.add(now.minusDays(i).format(formatter));
        }
        return last7DaysFormatted;
    }
    
    public static float getTotGiorno(DefaultTableModel vendite,String dataInserita){
        LocalDate dataScelta = LocalDate.parse(dataInserita, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        //System.out.println(dataScelta.toString());
        float tot = 0;
        
        for(int i=0;i<vendite.getRowCount();i++){
            //LocalDateTime data = (LocalDateTime) vendite.getValueAt(i, 3);
            String rowDate = vendite.getValueAt(i, 3).toString();
            LocalDateTime data = LocalDateTime.parse(rowDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            
            if(data.getDayOfYear() == dataScelta.getDayOfYear() && data.getMonthValue() == dataScelta.getMonthValue() && data.getYear() == dataScelta.getYear()){
                float rowTot = (float) vendite.getValueAt(i, 1);
                tot += rowTot;
            }
        }
        return tot;
    }

    public static ArrayList<String> getWeeksFromStartDateToToday(){
        LocalDate startDate = LocalDate.of(2018, 1, 1);
        ArrayList<String> weeks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
         LocalDate currentEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));

        while (!currentEnd.isBefore(startDate)) {
            LocalDate currentStart = currentEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY));
                if (currentStart.isBefore(startDate)) {
                    currentStart = startDate;
                }

            String weekRange = currentStart.format(formatter) + " al " + currentEnd.format(formatter);
            weeks.add(weekRange);
            currentEnd = currentEnd.minusWeeks(1);
    }

    return weeks;
    }
    
    //Per modificare i colori in ogni barra 
    static class CustomBarRenderer extends BarRenderer {
        @Override
        public Paint getItemPaint(int row, int column) {
            int minBrightness = 120; // Minimum brightness value for contrast
            int red = minBrightness + (int) (Math.random() * (255 - minBrightness));
            int green = minBrightness + (int) (Math.random() * (255 - minBrightness));
            int blue = minBrightness + (int) (Math.random() * (255 - minBrightness));
        return new Color(red, green, blue);
        }
    }
    
    public static String getTotProdottiVendutiGiornata(DefaultTableModel model){
        LocalDate dataScelta = LocalDate.parse(LocalDate.now().toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int totaleProdottiVenduti = 0;
        for(int i=0;i<model.getRowCount();i++){
            if(model.getValueAt(i, 3).toString().substring(0, 10).contains(dataScelta.toString())){
                ArrayList<Prodotto> prodotti = (ArrayList<Prodotto>) model.getValueAt(i, 2);
                for(Prodotto p : prodotti){
                    totaleProdottiVenduti += p.getQuantita();
                }
            }
        }
        System.out.println(dataScelta);
        return Integer.toString(totaleProdottiVenduti);
    }
    
}


