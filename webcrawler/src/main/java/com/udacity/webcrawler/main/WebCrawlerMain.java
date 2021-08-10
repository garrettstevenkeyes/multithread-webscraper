package com.udacity.webcrawler.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty)

    if (config.getResultPath().isEmpty()){
      System.out.println("The result path was empty, printing to terminal");
      Writer printedStream = new BufferedWriter(new OutputStreamWriter(System.out));
      resultWriter.write(printedStream);
    } else {
      String resultPathString = config.getResultPath();
      resultWriter.write(Paths.get(resultPathString));
    }

    try {
      Writer writer = new FileWriter(config.toString());
      resultWriter.write(writer);
    } catch (IOException exception) {
//    // TODO: Write the profile data to a text file (or System.out if the file name is empty)
      Writer writer = new FileWriter("path/to/file.txt");
      resultWriter.write(writer);
    }


  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
