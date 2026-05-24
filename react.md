# React Router

## Routing Framework in React

### Components `BrowserRouter`, `Routes` and `Route`

`<BrowserRouter>`, `<Routes>` and `<Route>` are the three components from the `react-router-dom` library that work together and are nested in that order.

`<BrowserRouter>`

It is the root component of the routing system. Its function is to listen to browser URL changes (using the HTML5 History API) and provide that information to the rest of the application through React contexts. It does not render anything visible by itself. It usually wraps the whole application or at least the part that needs routing.

`<Routes>`

It acts as a container and route selector. It examines the current URL and determines which of its child `<Route>` elements should be rendered. It activates only one route at a time, the one that best matches the URL. It is the equivalent of a `switch` applied to routes.

`<Route>`

It defines the association between a URL and a component. It has two main attributes:

- `path` - the URL pattern that must match.
- `element` - the component that is rendered when the route matches.

The final structure would look something like this:

```jsx
function SimpleRoutes() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={<PaginaPrincipal />}
          />
          <Route path="primerapagina" element={<PrimeraPagina />} />
          <Route path="segundapagina" element={<SegundaPagina />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}
```

The relationship is hierarchical and necessary: `<Route>` only works inside `<Routes>`, and `<Routes>` only works inside `<BrowserRouter>`.

In the context of pure React (as in the exercises from the syllabus), routing is the client's responsibility.

## Links to other pages. `Link` component

To perform controlled navigation, that is, in SPA format without accessing the server, the `Link` component is used. The fundamental attribute of the `Link` component is `to`, which indicates the route that will be navigated to when the link is selected. It is the equivalent of the `href` attribute of the HTML `<a>` element.

```jsx
import { BrowserRouter, Routes, Route, Link } from 'react-router'

import PaginaPrincipal from './PaginaPrincipal'
import PrimeraPagina from './PrimeraPagina'
import SegundaPagina from './SegundaPagina'

function SimpleRoutesWithLinks() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={
              <>
                <h2>Bienvenido a la página principal</h2>
                <p>
                  En <Link to="/primerapagina">/primerapagina</Link> se mostrará el contenido asociado a esa ruta.
                </p>
                <p>
                  En <Link to="/segundapagina">/segundapagina</Link> se mostrará el contenido asociado a otra ruta.
                </p>
              </>
            }
          />
          <Route path="primerapagina" element={<PrimeraPagina />} />
          <Route path="segundapagina" element={<SegundaPagina />} />
        </Routes>
      </BrowserRouter>
    </>
  )
}

export default SimpleRoutesWithLinks;
```

## Links to other pages with awareness of activity. `NavLink` component

There is a second link component in React Router, `NavLink`, which differs from `Link` in that it can tell whether it is active or not. The link is active when it matches the URL in the browser navigation window.

When React Router detects that a `NavLink` is active, it adds the `active` class to it, and that style can be defined in a CSS file imported on that page.

With those characteristics, `NavLink` components are usually used for navigation bars in which there are several links and the current page link is highlighted, so the user has a clearer idea of where they are.

```jsx
// SimpleRoutesWithNavLinks.jsx
import { BrowserRouter, Routes, Route, NavLink } from 'react-router'

import TableroNaviLinks from './TableroNavLinks'
import PrincipalNavLinks from './PrincipalNavLinks'
import PrimeraPagina from './PrimeraPagina'
import SegundaPagina from './SegundaPagina'

function SimpleRoutesWithNavLinks() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PrincipalNavLinks />} />
        <Route path="primerapagina" element={<PrimeraPagina />} />
        <Route path="segundapagina" element={<SegundaPagina />} />
      </Routes>
    </BrowserRouter>
  )
}

export default SimpleRoutesWithNavLinks;

// PrincipalNavLinks.jsx
import NavegacionNavlinks from "./NavegacionNavLinks";

function PrincipalNavLinks() {
  return (
    <>
      <NavegacionNavlinks />
      <h2>Esta es la página principal</h2>
      <p>El contenido de esta página se muestra al acceder a la ruta /.</p>
    </>
  )
}

export default PrincipalNavLinks;
```

## Index routes

In the previous examples, the default route has been specified by giving the root folder value to the `path` attribute (`path="/"`). Another way to do it is by indicating the `index` attribute (without an associated value) instead of the `path` attribute:

```jsx
function SimpleRoutes() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route index element={<PaginaPrincipal />} />
          <Route path="primerapagina" element={<PrimeraPagina />} />
          <Route path="segundapagina" element={<SegundaPagina />} />
        </Routes>
      </BrowserRouter>
    </>
  )
}
```

## Nested routes in React Router

With the previous elements (`BrowserRouter`, `Routes`, `Route`, `Link` and `NavLink`) we can do SPA navigation through the application in a simple way. However, we can compose `Route` elements to achieve a better page structure and to use common elements across several pages without repeating them.

To illustrate the difference between non-nested and nested routes, suppose an example with dashboard pages:

```txt
// Without nested routes each route is independent
/tablero               → <Tablero />
/tablero/usuarios      → <Usuarios />      // Tablero disappears
/tablero/estadisticas  → <Estadísticas />  // Tablero disappears

// With nested routes the parent persists
/tablero          → <Tablero />
/tablero/usuarios  → <Tablero /> + <Usuarios /> inside
/tablero/estadisticas  → <Tablero /> + <Estadísticas /> inside
```

This is useful for layout templates: a sidebar, a navigation menu, a header... that stays while the inner content changes.

A very simple first example would have a route at the root path and a nested route inside it:

```jsx
import { BrowserRouter, Routes, Route } from "react-router";

import MarketingLayout from "./MarketingLayout";
import MarketingHome from "./MarketingHome";
import Contact from "./Contact";

function NestedRoutesWithoutPath() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route element={<MarketingLayout />}>
            <Route index element={<MarketingHome />} />
            <Route path="contact" element={<Contact />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default NestedRoutesWithoutPath;
```

At the root path only the `MarketingHome` content would be shown, and at `/contact` the `MarketingHome` content and the `Contact` content would be shown.

If the external component in the nesting has a value for the `path` attribute, that value will be added to the nested components inside it.

In the following example:

```jsx
import { BrowserRouter, Routes, Route } from "react-router";

import ProjectsHome from "./ProjectsHome";
import ProjectsLayout from "./ProjectsLayout";
import Project from "./Project";
import EditProject from "./EditProject";

function NestedRoutesWithPath() {
  return (
    <>
      <BrowserRouter>
        <Routes>
        <Route path="projects">
          <Route index element={<ProjectsHome />} />
          <Route element={<ProjectsLayout />}>
            <Route path="project" element={<Project />} />
            <Route path="project/edit" element={<EditProject />} />
          </Route>
        </Route>
      </Routes>
      </BrowserRouter>
    </>
  );
}

export default NestedRoutesWithPath;
```

at the route `/projects` the `<ProjectsHome>` component would be shown. At the route `/projects/project` for example, the `<ProjectsLayout>` component would be shown and, after that, the `<Project>` component. And at the route `/projects/project/edit` the `<ProjectsLayout>` component would be shown and, after that, the `<EditProject>` component.

### Multiple nesting

Route nesting can have as many levels as needed, not just one:

```jsx
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<DisposicionRaiz />}>
          <Route path="tablero" element={<Tablero />}>
            <Route path="usuarios/:id" element={<DetalleDeUsuario />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
```

The URL `/tablero/usuarios/42` renders:

```jsx
<DisposicionRaiz>
  <Tablero>
    <DetalleDeUsuario userId="42" />
  </Tablero>
</DisposicionRaiz>
```

## Displaying nested components. `Outlet` component

In the previous nested route examples it has been explained that nested routes show the outer component and the nested component. However, that is not the default behavior; it was done that way to make the examples clearer. The default behavior is that the nested components are not shown.

The parent component can decide where to show the nested components with the `<Outlet />` component. In one of the previous examples, the content of the parent component, `ProjectsLayout`, is:

```jsx
import { Outlet } from "react-router";
function ProjectsLayout() {
  return (
    <>
      {/* The HTML <p> element is always shown */}
      <p>This is the content of ProjectsLayout</p>
      {/* The child appears here according to the URL */}
      <Outlet />
    </>
  );
}

export default ProjectsLayout;
```

If the URL is `/projects/project`, `<Project />` appears in the place of `<Outlet />`. If it is `/projects/project/edit`, `<EditProject />` appears. The `<p>` always appears.

The scheme is that the parent defines the structure, `<Outlet />` is the slot where the child component will appear, and React Router decides which child matches the URL. `<Outlet />` is a placeholder that tells React Router: "render here the child component that corresponds to the current URL". Without it, the parent would not know where to place its children.

You can think of `<Outlet />` as React's `{children}`, but controlled by the URL instead of by the parent component. The parent defines the structure, React Router decides which child matches, and `<Outlet />` is the meeting point between both.

## Structured pages with nested routes and `Outlet`

A typical SPA application structure has a common part that repeats in all or many pages and a variable part. For example, Navbar and Sidebar can be fixed and only the central content changes according to the URL. With nested routes, the common structure can be provided and with `<Outlet />` the variable part is indicated.

```txt
┌─────────────────────────────────┐
│  Navbar                         │
├──────────┬──────────────────────┤
│          │                      │
│  Sidebar │   Variable content   │
│          │                      │
└──────────┴──────────────────────┘
```

## How it works internally

When React Router sees the URL `/tablero/usuarios`, it looks for which routes match in nesting order:

```txt
/ → <DisposiciónRaiz />
  /tablero → <Tablero />
    /tablero/usuarios → <Usuarios />
```

Then it renders from outside to inside, and each `<Outlet />` it finds is replaced by the next level:

```jsx
// React Router does this internally, simplified:
<RootLayout>
  {/* RootLayout's <Outlet /> becomes: */}
  <Dashboard>
    {/* Dashboard's <Outlet /> becomes: */}
    <Users />
  </Dashboard>
</RootLayout>
```

Each component in the chain does not know which child it has, it only knows that it has a hole where the `Outlet` component is, and React Router decides which concrete component to put there.

If the `Outlet` component is not used in the parent component, the children are not drawn.

```jsx
// ❌ Without Outlet — children never appear
function Dashboard() {
  return (
    <div>
      <Sidebar />
      {/* No hole → Users and Stats never render */}
    </div>
  )
}

// ✅ With Outlet — the active child appears where you choose
function Dashboard() {
  return (
    <div>
      <Sidebar />
      <Outlet />  {/* Users, Stats, or DashboardHome will appear here */}
    </div>
  )
}
```

The `Outlet` can go anywhere in the JSX, it does not have to be at the end. You can structure the component layout as desired:

```jsx
function Dashboard() {
  return (
    <div className="app">
      <header>
        <Navbar />
      </header>
      <div className="body">
        <aside>
          <Sidebar />
        </aside>
        <main>
          <Outlet />   {/* The child goes here, in the main area */}
        </main>
      </div>
      <footer>
        <Footer />
      </footer>
    </div>
  )
}
```

See example `NestedRoutesWithNavLinks.jsx`.

## Passing context through `Outlet`

If the parent needs to share data with the child, it can do so through the `context` prop of `Outlet`, without direct props or the Context API:

```jsx
// In the parent
const [usuario, setUsuario] = useState({ nombre: 'Ana', rol: 'admin' })
const [permiso, setPermiso] = useState(["consulta", "exportacion"])
<Outlet context={{ usuario, permisos }} />

// In the child
import { useOutletContext } from 'react-router-dom'

function Usuarios() {
  const { usuario, permisos } = useOutletContext()
  // ...
}
```

Any child, no matter how many nesting levels there are, can read that context with the `useOutletContext` hook:

```jsx
function Users() {
  const { usuario } = useOutletContext()

  return <p>Conectado como: {usuario.nombre}</p>
}
```

This is useful for data that is relevant to an entire section of the application, without polluting global state.

## What renders when there is no active child

If the URL is exactly `/dashboard` and you do not have an index route, the `<Outlet />` renders `null`: the slot appears empty. That is why index routes exist, to cover that case:

```jsx
<Route path="dashboard" element={<Dashboard />}>
  <Route index element={<DashboardHome />} />  {/* covers exact /dashboard */}
  <Route path="users" element={<Users />} />
</Route>
```

## Route prefixes

A `Route` component that has a value for the `path` attribute but no `element` attribute serves to add a path segment to the internal components' route, but without adding a parent component:

```jsx
<Route path="projectos">
  <Route index element={<InicioProjectos />} />
  <Route element={<DisposicionProjectos />}>
    <Route path=":pid" element={<Projecto />} />
    <Route path=":pid/edit" element={<ModificarProjectos />} />
  </Route>
</Route>
```

## Dynamic segments

If a route segment starts with `:`, it becomes a dynamic segment. When the route matches the URL, the dynamic segment is available through the `params` variable for other router APIs such as `useParams`:

```jsx
<Route path="equipos/:equipoId" element={<Equipo />} />

//app/team.jsx
import { useParams } from "react-router";

export default function Equipo() {
  let params = useParams();
  // params.teamId
}
```

There can be several dynamic segments in one route:

```jsx
<Route
  path="/c/:categoryId/p/:productId"
  element={<Product />}
/>
```

and each one can be accessed individually:

```jsx
import { useParams } from "react-router";

export default function CategoryProduct() {
  let { categoryId, productId } = useParams();
  // ...
}
```

## Optional segments

A route segment can be made optional by adding a `?` at the end of the segment.

```jsx
<Route path=":idioma?/categorias" element={<Categorias />} />
```

There can also be optional static segments:

```jsx
<Route path="usuarios/:idUsuario/modificar?" element={<Usuario />} />
```

## Additional `NavLink` properties

It has been explained that `NavLink`s are links that are important to highlight when they are active. If they are active, the `active` class is automatically added so that a style can be applied to highlight them. To make styling easier, there are other attributes that can be used with `NavLink`s: `class`, `style`, and `children`.

```jsx
// className
<NavLink
  to="/messages"
  className={({ isActive }) =>
    isActive ? "text-red-500" : "text-black"
  }
>
  Messages
</NavLink>
// style
<NavLink
  to="/messages"
  style={({ isActive }) => ({
    color: isActive ? "red" : "black",
  })}
>
  Messages
</NavLink>
// children
<NavLink to="/message">
  {({ isActive }) => (
    <span className={isActive ? "active" : ""}>
      {isActive ? "👉" : ""} Tasks
    </span>
  )}
</NavLink>
```

## The `useNavigate` hook

The `useNavigate` hook allows the programmer to navigate the program to a new page without requiring user interaction. It has a similar effect to changing the `window.location.href` property in JavaScript.

```jsx
import { useNavigate } from "react-router";

export function PaginaInicioSesion() {
  let navigate = useNavigate();

  return (
    <>
      <MiCabecera />
      <MiFormularioInicioSesion
        onSuccess={() => {
          navigate("/tablero");
        }}
      />
      <MiPie />
    </>
  );
}
```

For formal navigation, it is better to use `Link` or `NavLink`, since they provide a better user experience, keyboard events, labeling for accessibility, opening in a new tab or window, right-click context menus, etc. Their use should be restricted to situations where navigation to the new page is not a direct consequence of the user's action:

- After submitting a form.
- Logging out due to inactivity.
- Timed user interfaces, such as puzzles, etc.

## Search parameters

In addition to dynamic route parameters, React components have access to search parameters, which are the key=value pairs that appear after a question mark (`?`) at the end of the URL path, and if there are several they are separated by `&`:

```txt
https://www.tiendaonline.es/s?busq=cuberteria&seccion=hogar
```

Components can access those parameters using the `useSearchParams` hook, which returns an instance of `URLSearchParams`:

```jsx
function BuscarResultados() {
  let [parametrosBusqueda] = useSearchParams();
  return (
    <div>
      <p>
        Has buscado <i>{parametrosBusqueda.get("busq")}</i>
      </p>
      <ResultadosBusqueda />
    </div>
  );
}
```

## Detailed behavior of navigation in React Router

When `history.pushState()` changes the URL, `<BrowserRouter>` receives the event, updates its internal state with the new location, and re-renders. That re-render reaches `<Routes>`, which at that moment runs the matching algorithm:

```txt
Current URL: /dashboard/settings

Does "/"          match?  → could, but keep searching
Does "/login"     match?  → no
Does "/dashboard" match?  → yes, and it has children → go inside
  Does "/settings"? → yes → render <Settings />
```

It is not a simple string comparison — React Router uses a ranking algorithm that scores each route according to its specificity.

### Route matching scores

So, `/users/42` always beats `/users/:id`, which always beats `*`, regardless of the order in which you declare them in the JSX.

## Downloading data to show the content of a new page

When React Router has to mount a new component after following a `Link` or a `NavLink`, it does not need to download the component information from the server, because those are components that are already in the bundle downloaded at startup. The component change is purely local; React Router simply mounts and unmounts. No HTTP request is involved in that step.

However, apart from the component itself, the JSX code and logic that are already in the browser since the app loaded and that do not need network loading, the data that that component needs to show may require a request to the server.

### Lifecycle of component changes

```text
Lifecycle of component changes
```

## Ways to download dynamic data

Downloading dynamic data from the server is a decision made by the component, not by the router. There are three common strategies:

### Fetch on mount

This is the most common pattern: the component appears, and in its `useEffect` it launches the request. There is a moment when it is shown without data (skeleton, spinner).

```jsx
function Dashboard() {
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('/api/dashboard').then(r => r.json()).then(setData);
  }, []);

  if (!data) return <Spinner />;
  return <DashboardView data={data} />;
}
```

### Data already in cache

If `react-query` or SWR is used, the second time you visit `/dashboard` the data is already in memory. The component mounts with data immediately, without waiting for any request. The request may still be launched in the background to revalidate, but the user does not see a loading state.

### React Router 6.4+ loaders

The modern version of React Router allows a loader to be declared alongside the route, and React Router executes it before doing the swap. The component does not mount until the data is available:

```jsx
// The router knows that before rendering Dashboard, it must execute this:
const router = createBrowserRouter([
  {
    path: '/dashboard',
    element: <Dashboard />,
    loader: () => fetch('/api/dashboard').then(r => r.json()),
  }
]);

// The component receives the data already ready, without useEffect or spinner:
function Dashboard() {
  const data = useLoaderData();
  return <DashboardView data={data} />;
}
```

With loaders, navigation pauses briefly until the data arrives, and then the component change happens with the component already ready, without flickers or loading states inside the component.

## Sources

React Router. API Reference. Installation. Declarative mode.
