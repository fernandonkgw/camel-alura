package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				String diretorio = "/home/developer/workspace/camel-alura/pedidos"; // poderia ser apenas "pedidos"
				
				from("file:" + diretorio + "?delay=5s&noop=true")
					.routeId("rota-pedidos")
					.multicast()
						.to("direct:soap")
						.to("direct:http");
				
				from("direct:http")
					.routeId("rota-http")
					.setProperty("pedidoId", xpath("/pedido/id/text()"))
					.setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()"))
					.split()
						.xpath("/pedido/itens/item")
					.filter()
						.xpath("/item/formato[text()='EBOOK']")
					.setProperty("ebookId", xpath("/item/livro/codigo/text()"))
					.marshal()
						.xmljson()
					.log("${id}")
					.log("${body}")
					.setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
					.setHeader(Exchange.HTTP_QUERY, simple("clienteId=${exchangeProperty.clienteId}&pedidoId=${exchangeProperty.pedidoId}&ebookId=${exchangeProperty.ebookId}"))
				.to("http4://localhost:8080/webservices/ebook/item");
				
				from("direct:soap")
				.routeId("rota-soap")
				.log("chamando serviço soap ${body}")
				.to("mock:soap");
			}
		});
		
		context.start(); // aqui o camel inicia o trabalho
		
		ProducerTemplate producer = context.createProducerTemplate();
	    producer.sendBody("direct:soap", "<pedido> ... </pedido>");
		
		Thread.sleep(3000); // espera um pouco para terminar a rota
		context.stop();
	}	
}
