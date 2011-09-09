/*
 * Copyright (c) 1998-2011 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.jboss.arquillian.container.resin.embedded_4;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import com.caucho.resin.HttpEmbed;
import com.caucho.resin.ResinEmbed;
import com.caucho.resin.WebAppEmbed;
import com.caucho.server.dispatch.ServletManager;
import com.caucho.server.webapp.WebApp;

/**
 * Resin 4 embedded container adapter for Arquillian.
 * 
 * @author Reza Rahman
 * @version $Revision: $
 */
public class ResinEmbeddedContainer implements
    DeployableContainer<ResinEmbeddedConfiguration> {
  
  private static final Logger log = 
    Logger.getLogger(ResinEmbeddedContainer.class.getName());

  private ResinEmbed _resin;
  private ResinEmbeddedConfiguration _configuration;

  private File _workingDirectory;

  @Inject
  @DeploymentScoped
  private InstanceProducer<WebAppEmbed> _webApplicationProducer;

  /**
   * @see DeployableContainer#getConfigurationClass()
   */
  @Override
  public Class<ResinEmbeddedConfiguration> getConfigurationClass()
  {
    return ResinEmbeddedConfiguration.class;
  }

  /**
   * @see DeployableContainer#getDefaultProtocol()
   */
  @Override
  public ProtocolDescription getDefaultProtocol()
  {
    return new ProtocolDescription("Servlet 3.0");
  }

  /**
   * @see DeployableContainer#setup(org.jboss.arquillian.container.spi.client.container.ContainerConfiguration)
   */
  @Override
  public void setup(ResinEmbeddedConfiguration configuration)
  {
    _configuration = configuration;
  }

  /**
   * @see DeployableContainer#start()
   */
  @Override
  public void start() 
    throws LifecycleException
  {
    try {
      createWorkingDirectory();
    }
    catch (Exception e) {
      throw new LifecycleException("Failed to create temporary directory for Resin 4 embedded container.",e);
    }

    try {
      if (_configuration.getConfigurationFile() == null) {
        _resin = new ResinEmbed();
      } else {
        _resin = new ResinEmbed(_configuration.getConfigurationFile());
      }
        
      _resin.setRootDirectory(_workingDirectory.getAbsolutePath());
      _resin.addPort(new HttpEmbed(_configuration.getHttpPort()));
  
      log.info(String.format("Starting Resin 4 embedded container [%s] from working directory %s.",  _resin.hashCode(), _workingDirectory.getAbsolutePath()));
      
      _resin.start();
    } catch (Exception e) {
      throw new LifecycleException(String.format("Failed to start Resin 4 embedded container [%s].", _resin.hashCode()), e);
    }
  }

  /**
   * @see DeployableContainer#stop()
   */
  @Override
  public void stop() 
    throws LifecycleException
  {
    log.info(String.format("Stopping Resin 4 embedded container [%s].", _resin.hashCode()));

    try {
      _resin.stop();
      _resin.destroy();
    } catch (Exception e) {
      throw new LifecycleException(String.format("Failed to stop Resin 4 embedded container [%s].", _resin.hashCode()), e);
    }
    
    try {
      removeWorkingDirectory();
    } catch (IOException e) {
      throw new LifecycleException(String.format("Failed to remove temporary directory %s for Resin 4 embedded container [%s].", _workingDirectory.getAbsolutePath(), _resin.hashCode()), e);
    }
  }

  /**
   * @see DeployableContainer#deploy(Archive)
   */
  @Override
  public ProtocolMetaData deploy(Archive<?> archive) 
    throws DeploymentException
  {
    try {
      log.info(String.format("Deploying web archive %s to Resin 4 embedded container [%s].", archive.getName(), _resin.hashCode()));

      // Resin needs an explicit context path.
      String contextPath = getContextPath(archive);

      // Resin needs a directory to explode .war contents to.
      File deploymentDirectory = createDeploymentDirectory(contextPath);

      // The .war must be written out to the file system for Resin.
      File warFile = new File(_workingDirectory, archive.getName());
      if (warFile.exists()) {
        warFile.delete();
      }

      ZipExporter exporter = archive.as(ZipExporter.class);
      exporter.exportTo(warFile.getAbsoluteFile());

      WebAppEmbed webApplication = new WebAppEmbed();
      webApplication.setRootDirectory(deploymentDirectory.getAbsolutePath());
      webApplication.setArchivePath(warFile.getAbsolutePath());
      webApplication.setContextPath(contextPath);

      _resin.addWebApp(webApplication);

      _webApplicationProducer.set(webApplication);

      // Creating meta-data for Arquillian.
      HTTPContext httpContext = new HTTPContext("localhost", _configuration.getHttpPort());
      
      WebApp deployedWebApplication = webApplication.getWebApp();
      ServletManager servletManager = deployedWebApplication.getServletMapper().getServletManager();
      Map<String, ? extends ServletConfig> servlets = servletManager.getServlets();

      for (String name : servlets.keySet()) {
        ServletConfig servetConfiguration = servlets.get(name);
        httpContext.add(new Servlet(name, servetConfiguration.getServletContext().getContextPath()));
      }

      return new ProtocolMetaData().addContext(httpContext);
    } catch (Exception e) {
      throw new DeploymentException(String.format("Failed to deploy web archive %s to Resin 4 embedded container [%s].", archive.getName(), _resin.hashCode()), e);
    }
  }

  /**
   * @see DeployableContainer#undeploy(Archive)
   */
  public void undeploy(Archive<?> archive) 
    throws DeploymentException
  {
    log.info(String.format("Undeploying web archive %s from Resin 4 embedded container [%s].", archive.getName(), _resin.hashCode()));

    try {
      _resin.removeWebApp(_webApplicationProducer.get());
    } catch (RuntimeException e) {
      throw new DeploymentException(String.format("Failed to undeploy web archive %s to Resin 4 embedded container [%s].", archive.getName(), _resin.hashCode()), e);
    }
  }

  /**
   * @see DeployableContainer#deploy(Descriptor)
   */
  @Override
  public void deploy(Descriptor descriptor) throws DeploymentException
  {
    throw new UnsupportedOperationException("Resin does not support resource files. Please use resin.xml, web.xml, resin-web.xml, beans.xml or resin-beans.xml to deploy resources.");
  }

  /**
   * @see DeployableContainer#undeploy(Descriptor)
   */
  @Override
  public void undeploy(Descriptor descriptor) throws DeploymentException
  {
    throw new UnsupportedOperationException("Resin does not support resource files. Please use resin.xml, web.xml, resin-web.xml, beans.xml or resin-beans.xml to deploy resources.");
  }

  private void createWorkingDirectory() throws IOException
  {
    _workingDirectory = new File("resin-work-" + UUID.randomUUID().toString()); //File.createTempFile("arquillian", "resin");

    _workingDirectory.delete();
    _workingDirectory.mkdirs();
  }

  private void removeWorkingDirectory() throws IOException
  {
    deleteFile(_workingDirectory);
  }

  private static void deleteFile(File file) throws IOException
  {
    if (file.isDirectory()) {
      deleteDirectory(file);
    }

    if (file.delete() == false) {
      throw new IOException("Failed to delete " + file);
    }
  }

  private static void deleteDirectory(File directory) throws IOException
  {
    File[] files = directory.listFiles();

    if (files == null) {
      throw new IOException("Error listing files for " + directory);
    }

    for (File file : files) {
      deleteFile(file);
    }
  }

  private File createDeploymentDirectory(String contextPath)
  {
    File deploymentDirectory = new File(_workingDirectory,
                                        String.format("%s.%s", 
                                                      contextPath.substring(1),
                                                      UUID.randomUUID()));

    deploymentDirectory.mkdirs();

    return deploymentDirectory;
  }

  private static String getContextPath(Archive<?> archive)
  {
    String name = archive.getName();

    // Strip file extension.
    if (name.contains(".")) {
      name = name.substring(0, name.lastIndexOf("."));
    }

    return "/" + name;
  }
}