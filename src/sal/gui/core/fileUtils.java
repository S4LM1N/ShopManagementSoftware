package sal.gui.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Salvatore Minasola
 */

public class fileUtils {
    public static void salvaInventario(DefaultTableModel model, String filePath) throws IOException 
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) 
        {
            for (int i = 0; i < model.getColumnCount(); i++) 
            {
                bw.write(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) 
                {
                    bw.write(",");
                }
            }
            bw.newLine();

            for (int i = 0; i < model.getRowCount(); i++) 
            {
                for (int j = 0; j < model.getColumnCount(); j++) 
                {
                    Object value = model.getValueAt(i, j);
                    bw.write(value != null ? value.toString() : "");
                    if (j < model.getColumnCount() - 1) 
                    {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }
    
    
    public static void salvaVendite(DefaultTableModel model, String filePath) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Colonne
            for (int i = 0; i < model.getColumnCount(); i++) {
                bw.write(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();

            // Write the data rows
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    if (j==2) { //Array di prodotti
                        if(value != null){
                            ArrayList<Prodotto> prodotti = (ArrayList<Prodotto>) model.getValueAt(i, 2);
                            bw.write(prodottiToString(prodotti));
                        }
                    } else if (j==3) {
                        LocalDateTime dateTime = LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        bw.write(dateFormatter.format(dateTime));
                    } else {
                        bw.write(value != null ? value.toString() : "");
                    }
                    if (j < model.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }
    
    
    
    public static DefaultTableModel getVendite(String filePath) throws FileNotFoundException, IOException{
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        DefaultTableModel model = new DefaultTableModel();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (isHeader) {
                    model.setColumnIdentifiers(values);
                    isHeader = false;
                } else {
                    Object[] row = new Object[values.length];
                    for (int i = 0; i < values.length; i++) {
                        switch (model.getColumnName(i)) {
                            case "Prodotti":
                                row[i] = stringToProdotti(values[i]);
                                break;
                            case "Data":
                                row[i] = LocalDateTime.parse(values[i], dateFormatter);
                                break;
                            case "Numero":
                                row[i] = Integer.valueOf(values[i]);
                                break;
                            case "Totale":
                                row[i] = Float.valueOf(values[i]);
                                break;
                            default:
                                row[i] = values[i];
                                break;
                        }
                    }
                    model.addRow(row);
                }
            }
        }
        
        return model;
        
    }
    
    public static DefaultTableModel getInventario(String filePath) throws FileNotFoundException, IOException{
        DefaultTableModel model = new DefaultTableModel();
        
         try (BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            boolean isHeader = true;
            
            while((line = br.readLine())!= null){
                String[] values = line.split(",");
                if (isHeader) {
                    model.setColumnIdentifiers(values);
                    isHeader = false;
                } else {
                    Object[] row = new Object[values.length];
                    for (int i = 0; i < values.length; i++) {
                        switch (model.getColumnName(i)){
                            case "Nome":
                                row[i] = values[i];
                                break;
                            case "Codice":
                                row[i] = values[i];
                                break;
                            case "Prezzo":
                                row[i] = Float.valueOf(values[i]);
                                break;
                            case "Quantita":
                                row[i] = Integer.valueOf(values[i]);
                                break;
                            default:
                                row[i] = values[i];
                                break;
                        }
                    }
                    model.addRow(row);
                }
            }
         }
         return model;
    }

    private static String prodottiToString(ArrayList<Prodotto> prodotti) {
        StringBuilder sb = new StringBuilder();
        if(prodotti != null){
            for (Prodotto prodotto : prodotti) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(prodotto.toString()); // Customize this if you need a specific format
            }
        }
        return sb.toString();
    }
    
    private static ArrayList<Prodotto> stringToProdotti(String value){
        ArrayList<Prodotto> prodotti = new ArrayList<>();
        String[] prodottiStringa = value.split(";");
        
        
        
        for(String prodotto : prodottiStringa){
            String[] attributo = prodotto.substring(1, prodotto.length() - 1).split("-");
            
            if(attributo.length == 4){
                Prodotto p = new Prodotto(attributo[0], attributo[1], Float.parseFloat(attributo[2]), Integer.parseInt(attributo[3]));
                prodotti.add(p);
            }
        }
        return prodotti;
    }
    
}
