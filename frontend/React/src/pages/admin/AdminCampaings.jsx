import '../css/campaigns.css';
import AdminMenuCardsList from '../MenuCards/CardsByRole/Admin/AdminMenuCardsList';
import GenericPageWrapper from '../generalModules/GenericPageWrapper';
import WelcomeBar from '../generalModules/GenericWelcomeBar';
import SecurePage from '../generalModules/SecurePage';
import { useState, useEffect, useRef } from 'react'

	const apiUrl = "http://localhost:8080";

	const volunteersEndpoint = "/api/voluntarios";
	const partnerEntitiesEndpoint = "/api/partner-entities";


function AdminCampaigns() {

  return(
    <SecurePage>
			<GenericPageWrapper>
				<div className='page-header'>
					<nav>
						<Link className="back-link-inline" to="/admin">← Volver al panel</Link>	
					</nav>
						<h1>Campañas</h1>
						<p>Operaciones CRUD sobre las Campañas</p>
				</div>
				{partnerEntitiesEndpoint}
			</GenericPageWrapper>
		</SecurePage>
  )
}































