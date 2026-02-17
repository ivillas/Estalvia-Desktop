import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.ProducteServiceClient;

public class Productes {

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Productes window = new Productes();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Productes() {
        initialize();
        cargarDatosComparativa();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Comparador de Precios - Estalvia (Vista Comparativa)");
        frame.setBounds(100, 100, 900, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Definimos las columnas iniciales
        // Dinámicamente añadiremos los nombres de los supermercados después
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarDatosComparativa() {
        new Thread(() -> {
            try {
                // 1. Obtener datos del NAS
                List<ProductePreusDTO> productos = ProducteServiceClient.getProductes();

                // 2. Identificar todos los supermercados únicos para crear las columnas
                Set<String> nombresSupermercados = new TreeSet<>();
                for (ProductePreusDTO p : productos) {
                    nombresSupermercados.addAll(p.precios.keySet());
                }

                // 3. Configurar las columnas de la tabla
                Vector<String> columnNames = new Vector<>();
                columnNames.add("Marca");
                columnNames.add("Producto");
                columnNames.addAll(nombresSupermercados); // Añade cada super como columna

                // 4. Llenar los datos
                Vector<Vector<Object>> data = new Vector<>();
                for (ProductePreusDTO p : productos) {
                    Vector<Object> row = new Vector<>();
                    row.add(p.marca);
                    row.add(p.nombre);

                    // Para cada columna de supermercado, buscamos si este producto tiene precio
                    for (String superNombre : nombresSupermercados) {
                        BigDecimal precio = p.precios.get(superNombre);
                        row.add(precio != null ? String.format("%.2f €", precio) : "-");
                    }
                    data.add(row);
                }

                // 5. Actualizar la UI en el hilo de despacho de eventos
                SwingUtilities.invokeLater(() -> {
                    tableModel.setDataVector(data, columnNames);
                });

            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error al conectar con el NAS: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
