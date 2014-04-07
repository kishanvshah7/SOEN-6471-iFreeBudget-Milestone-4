/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mjrz.fm.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class NProxySelector extends ProxySelector {

	private List<URI> failed = new ArrayList<URI>();

	private static Logger logger = Logger.getLogger(NProxySelector.class
			.getName());

	public List<Proxy> select(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException("Null uri");
		}

		String proxyHost = ZProperties.getProperty("PROXY.HOST");
		String proxyPort = ZProperties.getProperty("PROXY.PORT");

		List<Proxy> result = new ArrayList<Proxy>();
		if ((proxyHost == null || proxyHost.trim().length() == 0)
				|| (proxyPort == null || proxyPort.trim().length() == 0)) {
			result.add(Proxy.NO_PROXY);
		}
		else {
			SocketAddress proxyAddress = new InetSocketAddress(proxyHost,
					Integer.parseInt(proxyPort));
			Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
			result.add(proxy);
		}
		return result;
	}

	public void connectFailed(URI uri, SocketAddress address, IOException ex) {
		logger.error("Connect failed: " + uri + "*" + address + "*"
				+ ex.getMessage());
		failed.add(uri);
	}
}
