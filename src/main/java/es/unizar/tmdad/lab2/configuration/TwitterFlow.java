package es.unizar.tmdad.lab2.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;

import es.unizar.tmdad.lab2.domain.MyTweet;
import es.unizar.tmdad.lab2.domain.TargetedTweet;
import es.unizar.tmdad.lab2.service.TwitterLookupService;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan
public class TwitterFlow {
	
	@Autowired
	private TwitterLookupService twitterlookupService;
	
	@Bean
	public DirectChannel requestChannel() {
		System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
		return new DirectChannel();
	}


	// Tercer paso
	// Los mensajes se leen de "requestChannel" y se envian al método "sendTweet" del
	// componente "streamSendingService"
	@Bean
	public IntegrationFlow sendTweet() {

		GenericTransformer<Tweet, TargetedTweet> tweetTransform = (Tweet tweet) -> {

			List<String> retrieveQueries = twitterlookupService.getQueries()
					.stream()
					.filter(query -> tweet.getText().contains(query))
					.collect(Collectors.toList());
			
			return new TargetedTweet(new MyTweet(tweet), retrieveQueries);
		};
		Function<TargetedTweet, ?> tweetSplit = tweet ->{

			return tweet.getTargets()
					.stream()
					.map(query -> new TargetedTweet(tweet.getTweet(), query))
					.collect(Collectors.toList());
		};
		
		GenericTransformer<TargetedTweet, TargetedTweet> strongTextTransform = (TargetedTweet tweet) ->{ 

			String text = tweet.getTweet().getText().replace(tweet.getFirstTarget(), "<strong>" + tweet.getFirstTarget() + "</strong>");

			tweet.getTweet().setUnmodifiedText(text);

			return tweet;
			};
			
		return IntegrationFlows.from(requestChannel())
				// Filter --> asegurarnos que el mensaje es un Tweet
				.filter(object -> object instanceof Tweet)
				// Transform --> convertir un Tweet en un TargetedTweet con tantos tópicos como coincida
				.transform(tweetTransform)
				// Split --> dividir un TargetedTweet con muchos tópicos en tantos TargetedTweet como tópicos haya
				.split(TargetedTweet.class,  tweetSplit)
				// Transform --> señalar el contenido de un TargetedTweet
				.transform(strongTextTransform)
				// Se envía al método sendTweet del streamSendingService
				.handle("streamSendingService", "sendTweet")
				.get();

	}

}

// Segundo paso
// Los mensajes recibidos por este @MessagingGateway se dejan en el canal "requestChannel"
@MessagingGateway(name = "integrationStreamListener", defaultRequestChannel = "requestChannel")
interface MyStreamListener extends StreamListener {

}