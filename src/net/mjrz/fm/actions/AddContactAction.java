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
package net.mjrz.fm.actions;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Contact;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddContactAction {
	public AddContactAction() {

	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		try {
			Contact c = (Contact) request.getProperty("CONTACT");
			Boolean b = (Boolean) request.getProperty("EXISTING");

			FManEntityManager em = new FManEntityManager();
			ActionResponse resp = new ActionResponse();
			em.addContact(c, b);
			resp.setErrorCode(ActionResponse.NOERROR);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
