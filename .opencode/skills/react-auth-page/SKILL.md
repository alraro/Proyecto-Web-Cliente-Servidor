---
name: react-auth-page
description: How to create a new React page with authentication, table, and modals
---

## Steps to create a new React page

### 1. Create the component (`pages/<role>/XxxPage.jsx`)

```jsx
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import GenericPageWrapper from "../generalModules/GenericPageWrapper";
import GenericTable from "../generalModules/GenericTable";
import GenericModal from "../generalModules/GenericModal";
import SecurePage from "../generalModules/SecurePage";
import { authHeaders } from "../auth/authUtils";

const apiUrl = "http://localhost:8080";

function XxxPage() {
    const [data, setData] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch(`${apiUrl}/api/xxx`, { headers: authHeaders() })
            .then(r => r.json())
            .then(d => { setData(d); setIsLoading(false); })
            .catch(e => console.error(e));
    }, []);

    return (
        <SecurePage>
            <GenericPageWrapper>
                <div className="page-header">
                    <nav>
                        <Link className="back-link-inline" to="/role-menu">← Volver</Link>
                    </nav>
                    <h1>Title</h1>
                </div>

                <GenericTable
                    title="List"
                    headers={{ id: "ID", name: "Nombre" }}
                    data={data}
                    isLoading={isLoading}
                    addRowFunction={handleAdd}
                    editRowFunction={handleEdit}
                    deleteRowFunction={handleDelete}
                    itemName="Item"
                />

                <GenericModal title="Edit" fields={FIELDS} values={selected}
                    isOpen={isOpen} onClose={handleClose} onSubmit={handleSave} />
            </GenericPageWrapper>
        </SecurePage>
    );
}
```

### 2. Add route in `App.jsx`

```jsx
import XxxPage from './pages/<role>/XxxPage';

// Inside the appropriate role section:
<Route path="/role/xxx" element={<XxxPage />} />
```

### 3. Add card in menu (if applicable)

Add to the corresponding `*Cards.jsx` in `MenuCards/CardsByRole/<Role>/sections/`:

```jsx
gestionCards.push({
    icon: "📦",
    title: "My Page",
    description: "Description of the page.",
    link: "/role/xxx"
});
```

## Important

- All fetch calls must use `authHeaders()` — never build `Authorization: Bearer` manually
- Always wrap with `SecurePage` to protect the route
- Use `GenericTable` and `GenericModal` from `generalModules/` — don't build custom tables
