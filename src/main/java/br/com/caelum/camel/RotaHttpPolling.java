package br.com.caelum.camel;

import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;

public class RotaHttpPolling {

	public static void main(String[] args) throws Exception {
		
		final XStream xStream = new XStream();
		xStream.alias("negociacao", Negociacao.class);
		
		SimpleRegistry registro = new SimpleRegistry();
		registro.put("mysql", criaDataSource());
		
		CamelContext context = new DefaultCamelContext(registro);
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("timer:negociacoes?fixedRate=true&delay=1s&period=360s")
					.to("http4://argentumws.caelum.com.br/negociacoes?proxyAuthHost=proxysp.correiosnet.int&proxyAuthPort=80")
					.convertBodyTo(String.class)
					.unmarshal(new XStreamDataFormat(xStream))
					.split(body())
						.process(new Processor() {
						
							@Override
							public void process(Exchange exchange) throws Exception {
								Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
								exchange.setProperty("preco", negociacao.getPreco());
								exchange.setProperty("quantidade", negociacao.getQuantidade());
								String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
								exchange.setProperty("data", data);
							}
						})
					.setBody(simple("INSERT INTO negociacao (preco, quantidade, data) VALUES (${property.preco}, ${property.quantidade}, '${property.data}')"))
					.log("${body}")
					.delay(1000)
				.to("jdbc:mysql");
			}
		});
		
		context.start(); // aqui o camel inicia o trabalho
		Thread.sleep(3000); // espera um pouco para terminar a rota
		context.stop();
	}
	
	private static MysqlConnectionPoolDataSource criaDataSource() {
		MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setDatabaseName("camel");
		dataSource.setServerName("localhost");
		dataSource.setPort(3306);
		dataSource.setUser("root");
		dataSource.setPassword("123");
		return dataSource;
	}
}
