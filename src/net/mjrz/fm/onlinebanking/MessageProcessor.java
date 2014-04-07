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
package net.mjrz.fm.onlinebanking;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class MessageProcessor {

	private static Logger logger = Logger.getLogger(MessageProcessor.class
			.getName());

	public static String getOfxResponse(ONLBDetails details, OfxRequest request) {
		HttpsURLConnection conn = null;
		StringBuilder response = new StringBuilder();
		BufferedReader reader = null;
		try {
			String host = details.getUrl();
			String data = request.getRequestString();

			URL url = new URL(host);

			conn = (HttpsURLConnection) url.openConnection();

			conn.setRequestProperty("REQUEST_METHOD", "POST");
			conn.setRequestProperty("Content-Type", "application/x-ofx");
			conn.setRequestProperty("Content-Length",
					Integer.toString(data.length()));
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());

			reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			while (true) {
				int s = reader.read();
				if (s == -1)
					break;
				response.append((char) s);
			}

			conn.disconnect();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		finally {
			if (conn != null)
				conn.disconnect();
			if (reader != null)
				try {
					reader.close();
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
		}
		return response.toString();
	}

	// public static String getOfxResponse1(ONLBDetails details, OfxRequest
	// request) throws Exception {
	// PostMethod method = null;
	// try {
	// String host = details.getUrl();
	// HttpClient client = new HttpClient();
	//
	// method = new PostMethod(host);
	//
	// client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
	// new DefaultHttpMethodRetryHandler());
	//
	// method.setRequestHeader("Content-Type", "application/x-ofx");
	//
	// StringRequestEntity re = new
	// StringRequestEntity(request.getRequestString(), "application/x-ofx",
	// "UTF-8");
	//
	// method.setRequestEntity(re);
	//
	// client.executeMethod(method);
	//
	// int status = method.getStatusCode();
	// String response = null;
	// if(status == 200) {
	// byte[] responseBytes = method.getResponseBody();
	// response = new String(responseBytes);
	// }
	// return response;
	// }
	// catch(Exception e) {
	// String st = MiscUtils.stackTrace2String(e);
	// logger.error(st);
	// throw e;
	// }
	// finally {
	// method.releaseConnection();
	// }
	// }
}
