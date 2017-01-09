package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaMovimentacoes {

	public static void main(String[] args) throws Exception {
		
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("file:movimentacoes?noop=true")
				.to("xslt:movimentacao-para-html.xslt")
				.setHeader(Exchange.CONTENT_TYPE, constant("text/html"))
				.log("${body}")
				.end();
			}
		});
		context.start();
		Thread.sleep(3000); // espera um pouco para terminar a rota
		context.stop();
	}
}
