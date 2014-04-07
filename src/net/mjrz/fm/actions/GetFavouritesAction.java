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

import java.util.List;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetFavouritesAction {
	public GetFavouritesAction() {

	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		try {
			User u = request.getUser();
			FManEntityManager em = new FManEntityManager();
			List favs = em.getFavourites(u.getUid());
			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("FAVOURITES", favs);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
		finally {

		}
	}
}
