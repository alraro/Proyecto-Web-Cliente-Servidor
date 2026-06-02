import { Link } from "react-router";

function ErrorPage() {
    return (
        <div>
            <h1>Error 404: Página no encontrada</h1>
            <p>Lo sentimos, la página que estás buscando no existe.</p>
            <Link to="/"><button>Volver a la página principal</button></Link>
        </div>
    );
}

export default ErrorPage;