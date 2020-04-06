package org.transitclock.gtfs_rt_exporter.main;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.gtfs_rt_exporter.service.NewShapeEventReader;
import org.transitclock.gtfs_rt_exporter.service.NewShapeEventReaderByRouteFileImpl;
import org.transitclock.gtfs_rt_exporter.service.NewShapeEventReaderFileImpl;
import org.transitclock.gtfs_rt_exporter.service.VehicleCustomPositionReader;
import org.transitclock.gtfs_rt_exporter.service.VehicleCustomPositionReaderFileImpl;

import com.google.protobuf.Message;


@SpringBootApplication
@EnableAutoConfiguration
@RestController
@EnableScheduling
@ComponentScan({"org.transitclock.gtfs_rt_exporter"})
public class Starter {
	@Value("${vehicle.position.file}") 
	String vehiclePositionFile;
	@Value("${new.shape.event.file}") 
	String newSahpeEventFile;
	@Value("${new.shape.event.route.file}") 
	String newSahpeEventByRouteFile;
	/*
	 * Create readers
	 */
	@Bean VehicleCustomPositionReader createVehiclePositionReader()
	{
		return new VehicleCustomPositionReaderFileImpl(vehiclePositionFile);
	}
//	@Bean NewShapeEventReader createNewShapeEventReader()
//	{
//		return new NewShapeEventReaderFileImpl(newSahpeEventFile);
//	}
	
	@Bean NewShapeEventReader createNewShapeEventReader()
	{
		return new NewShapeEventReaderByRouteFileImpl(newSahpeEventByRouteFile);
	}
	public static void main(String[] args) {
		SpringApplication.run(Starter.class, args);
	}
	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}
	
	@Autowired
	org.transitclock.gtfs_rt_exporter.service.GtfsRealtimeExporterImpl objService;
	@Autowired
	MappingJackson2HttpMessageConverter jsonMessageConverter; 
	Logger logger=LogManager.getLogger(this.getClass());
	@RequestMapping(value = "/vehicles-update", method = RequestMethod.GET,produces={ MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public void getVehicleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		 Message message = objService.getFeed("vehicle");
		 if(message==null)
		 {
			 resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no data for vehicules");
		 }
		    if ((req.getParameter("debug")!=null)) {
		      resp.getWriter().print(message);
		    } else {
		      resp.setContentType("application/x-google-protobuf");
		      message.writeTo(resp.getOutputStream());
		    }
		
	}
	@RequestMapping(value = "/trip-update", method = RequestMethod.GET,produces={ MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public void getTripUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		 Message message = objService.getFeed("newShape");
		 if(message==null)
		 {
			 resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no data for new trips");
		 }
		    if ((req.getParameter("debug")!=null)) {
		      resp.getWriter().print(message);
		    } else {
		      resp.setContentType("application/x-google-protobuf");
		      message.writeTo(resp.getOutputStream());
		    }
		
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

		};
	}

}
