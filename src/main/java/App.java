

import java.util.List;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.ProducteServiceClient;


public class App {

	 public static void main(String[] args) {

	        try {
	            List<ProductePreusDTO> productos =
	                ProducteServiceClient.getProductes();

	            for (ProductePreusDTO p : productos) {
	                System.out.println(p.marca + " - " + p.nombre);
	                p.precios.forEach((sup, preu) ->
	                    System.out.println("   " + sup + ": " + preu + " €")
	                );
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

}
