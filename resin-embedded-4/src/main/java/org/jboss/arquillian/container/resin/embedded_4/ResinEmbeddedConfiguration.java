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
 */
package org.jboss.arquillian.container.resin.embedded_4;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * Resin 4 embedded container configuration.
 * 
 * @author Reza Rahman
 * @version $Revision: $
 */
public class ResinEmbeddedConfiguration implements ContainerConfiguration {
  private int httpPort = 8086;
  private String configurationFile;

  /**
   * @see ContainerConfiguration#validate()
   */
  @Override
  public void validate() throws ConfigurationException
  {
    // Nothing to validate.
  }

  /**
   * HTTP port embedded Resin listens on, default 8086.
   */
  public int getHttpPort()
  {
    return httpPort;
  }

  /**
   * HTTP port embedded Resin listens on, default 8086.
   */
  public void setHttpPort(int httpPort)
  {
    this.httpPort = httpPort;
  }

  /**
   * Location of Resin configuration file (resin.xml), a minimal internal one
   * used by default.
   */
  public String getConfigurationFile()
  {
    return configurationFile;
  }

  /**
   * Location of Resin configuration file (resin.xml), a minimal internal one
   * used by default.
   */
  public void setConfigurationFile(String configurationFile)
  {
    this.configurationFile = configurationFile;
  }
}