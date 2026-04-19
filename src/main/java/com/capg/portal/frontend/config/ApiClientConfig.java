package com.capg.portal.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.capg.portal.frontend.client.AuthorClient;
import com.capg.portal.frontend.client.DiscountClient;
import com.capg.portal.frontend.client.EmployeeClient;
import com.capg.portal.frontend.client.JobClient;
import com.capg.portal.frontend.client.PublisherClient;
import com.capg.portal.frontend.client.RoyaltyScheduleClient;
import com.capg.portal.frontend.client.SalesClient;
import com.capg.portal.frontend.client.StoreClient;
import com.capg.portal.frontend.client.TitleAuthorClient;
import com.capg.portal.frontend.client.TitleClient;

@Configuration
public class ApiClientConfig 
{
    // 1. Create the base RestClient pointing to your Backend (Laptop B)
    @Bean
    public RestClient customRestClient() 
    {
        return RestClient.builder()
                .baseUrl("http://localhost:8080") // The IP of your backend server
                .requestInterceptor(new BasicAuthInterceptor())
                .build();
    }

    // 2. Create the Proxy Factory. This acts as the "Engine" that will power our Interfaces.
    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClient customRestClient) 
    {
        RestClientAdapter adapter = RestClientAdapter.create(customRestClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }
    
    @Bean
    public JobClient jobClient(HttpServiceProxyFactory factory) 
    {
        return factory.createClient(JobClient.class);
    }
    
    @Bean
    public PublisherClient publisherClient(HttpServiceProxyFactory factory) {
        return factory.createClient(PublisherClient.class);
    }
    
    @Bean
    public AuthorClient authorClient(HttpServiceProxyFactory factory) {
        return factory.createClient(AuthorClient.class);
    }
    
    // ... inside ApiClientConfig class
    @Bean
    public StoreClient storeClient(HttpServiceProxyFactory factory) {
        return factory.createClient(StoreClient.class);
    }
    
    @Bean
    public DiscountClient discountClient(HttpServiceProxyFactory factory) {
        return factory.createClient(DiscountClient.class);
    }
    
    @Bean
    public RoyaltyScheduleClient royaltyScheduleClient(HttpServiceProxyFactory factory) {
        return factory.createClient(RoyaltyScheduleClient.class);
    }

    @Bean
    public TitleAuthorClient titleAuthorClient(HttpServiceProxyFactory factory) {
        return factory.createClient(TitleAuthorClient.class);
    }
    
    @Bean
    public SalesClient salesClient(HttpServiceProxyFactory factory) {
        return factory.createClient(SalesClient.class);
    }
    
    @Bean
    public TitleClient titleClient(HttpServiceProxyFactory factory) {
        return factory.createClient(TitleClient.class);
    }
    
    @Bean
    public EmployeeClient employeeClient(HttpServiceProxyFactory factory) {
        return factory.createClient(EmployeeClient.class);
    }
}