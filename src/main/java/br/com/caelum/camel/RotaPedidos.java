package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("file:pedidos?delay=5s&noop=true")
				.log("${id}")
				.marshal().xmljson()
				.log("${body}")
				.setHeader("CamelFileName", simple("${file:name.noext}.json"))
				.to("file:saida");
			}
		});
		
		context.start(); // aqui o camel inicia o trabalho
		Thread.sleep(3000); // espera um pouco para terminar a rota
		context.stop();
	}	
}
