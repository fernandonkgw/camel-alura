package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				String diretorio = "/home/developer/workspace/camel-alura/pedidos"; // poderia ser apenas "pedidos"
				
				from("file:" + diretorio + "?delay=5s&noop=true")
				.split()
					.xpath("/pedido/itens/item")
				.filter()
					.xpath("/item/formato[text()='EBOOK']")
				.marshal().xmljson()
				
				.setHeader(Exchange.FILE_NAME, simple("${file:name.noext}-${header.CamelSplitIndex}.json"))
				.log("${id}")
				.log("${body}")
				.to("file:saida");
			}
		});
		
		context.start(); // aqui o camel inicia o trabalho
		Thread.sleep(3000); // espera um pouco para terminar a rota
		context.stop();
	}	
}
