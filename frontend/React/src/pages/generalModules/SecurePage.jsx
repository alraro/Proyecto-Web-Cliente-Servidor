
import { Navigate } from 'react-router-dom';

function SecurePage({children}) {

    const canEnter = true; // TODO: Implement access control logic

    if (!canEnter) {
        return <Navigate to="/" replace />;
    }
    
    return (<>{children}</>);
}

export default SecurePage;