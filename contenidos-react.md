# Guía React — Contenidos de la Asignatura

Esta guía recoge todo el contenido de React visto en clase. Úsala como referencia canónica.

---

## 1. Creación y ejecución de un proyecto React

Para la creación y ejecución de proyectos React se usa `npm` y `Vite`.

`npm` es el gestor de paquetes por defecto de `Node.js` y se incluye con la instalación de `Node.js` (https://nodejs.org/es/download).

`Vite` es una herramienta de compilación frontend que permite trabajar con diferentes tecnologías (`.json`, `.vue`, `.tsx`, `.scss`, `.jsx`, `.sass`, ...).

Para crear un proyecto se ejecutan las siguientes instrucciones:

```
$ npm create vite@latest mi-proyecto -- --template react
$ cd mi-proyecto
$ npm install
$ npm run dev
```

La primera orden crea un proyecto Vite con una plantilla para trabajar en React en la carpeta `mi-proyecto`. La tercera orden instala las dependencias necesarias en la carpeta de trabajo. La última orden arranca el servidor de tal forma que se reinicia automáticamente cuando se modifica alguno de los archivos fuente.

Si se quisiera trabajar con TypeScript, se indicaría un template distinto:

```
$ npm create vite@latest mi-proyecto -- --template react-ts
```

### Estructura de un proyecto Vite + React

La estructura típica de un proyecto Vite + React es:

```
mi-proyecto/
├── public/      # Archivos estáticos
├── src/
│  ├── assets/  # Imágenes, fuentes, etc.
│  ├── App.jsx  # Componente raíz
│  └── main.jsx # Punto de entrada
├── index.html
├── package.json
└── vite.config.js
```

Vite se usa directamente con `npm create`, que descarga y ejecuta el instalador automáticamente sin necesidad de instalarlo antes de forma global. Vite queda instalado dentro del proyecto (en `node_modules`), como dependencia local. Esto es lo recomendado porque cada proyecto puede usar su propia versión.

El archivo principal del nuevo proyecto es `index.html`, que define un elemento `<div>` con `id="root"` e incluye como módulo el archivo `src/main.jsx`.

El archivo `src/main.jsx` contiene una llamada a la función `createRoot()` de React, a la que le pasa como parámetro una referencia al elemento `<div>` con `id="root"` definido en `index.html`. Con la ejecución de `createRoot()` se le dice al navegador que React toma el control a partir de ese momento. `createRoot()` designa como contenedor raíz de la aplicación React al elemento que se le pasa como parámetro y devuelve un objeto `root` con dos métodos:

- `root.render(<Componente />)` — renderiza o actualiza el árbol de componentes dentro del contenedor.
- `root.unmount()` — destruye el árbol React y limpia el contenedor.

Por defecto, el contenido que se renderiza en el componente raíz es:

```jsx
<StrictMode>
  <App />
</StrictMode>
```

El componente `App` incluirá todos los componentes gráficos que se mostrarán en la pantalla del navegador.

### Edición y depuración con MS Visual Studio Code

La instalación básica de MS Visual Studio Code está preparada para trabajar con proyectos React. Su funcionamiento se basa en las herramientas de las que dispone para JavaScript e incluye coloreado sintáctico del código y emparejamiento de paréntesis. También está disponible IntelliSense, que ofrece sugerencias y compleción de instrucciones, tipos y métodos.

Para depurar el lado cliente de un código React se puede usar el depurador integrado de JavaScript. Para ello hay que abrir la vista de ejecución y configuración y crear un nuevo archivo `launch.json`. En el menú que se ofrece al crear el archivo se selecciona la opción **Web App (Chrome)** y se tendrá un resultado similar a:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "chrome",
            "request": "launch",
            "name": "Iniciar Chrome para localhost",
            "url": "http://localhost:8080",
            "webRoot": "${workspaceFolder}"
        }
    ]
}
```

Hay que hacer un cambio en ese código: sustituir el puerto de la URL por el `3000`.

---

## 2. Descripción del interfaz de usuario

### Definición y uso de componentes

La base de la descripción de aplicaciones web en React son los **componentes**, elementos de interfaz de usuario reutilizables que combinan aspectos estructurales definidos en HTML con aspectos de funcionamiento definidos en JavaScript.

En React se puede definir un componente que genere código HTML:

```jsx
function TablaDeContenidos() {
  return (
    <article>
      <h1>Mi primer componente</h1>
      <ol>
        <li>Componentes: Construcción de bloques de IU</li>
        <li>Definición de un componente</li>
        <li>Uso de componentes</li>
      </ol>
    </article>
  );
}
```

Sintácticamente, la definición empieza por la palabra reservada `function`, seguida del nombre del componente, la lista de parámetros entre paréntesis y el cuerpo del componente entre llaves. En React es obligatorio que el nombre de los componentes empiece por una letra mayúscula y escribir las etiquetas HTML con todas las letras en minúscula, para diferenciarlos entre sí.

Una de las ventajas que tiene usar componentes React sobre código HTML es que los componentes son reutilizables y se pueden componer entre sí:

```jsx
function Perfil() {
  return (
    <img
      src="https://i.imgur.com/MK3eW3Am.jpg"
      alt="Katherine Johnson"
    />
  );
}

function GaleriaDePerfiles() {
  return (
    <section>
      <h1>Científicos admirables</h1>
      <Perfil />
      <Perfil />
      <Perfil />
    </section>
  );
}
```

### Importación y exportación de componentes

El código React se guarda en archivos con extensión `.js`. Los componentes pueden estar definidos en un solo archivo o en varios. Por defecto, un componente no es visible fuera de su archivo: para usarlo en otro archivo hay que exportarlo e importarlo.

Hay dos tipos de exportaciones:

- **Con nombre**: se usan cuando hay que exportar más de un componente de un fichero. Se exportan los nombres entre llaves.
- **Por defecto**: se puede usar cuando solo se exporta un componente de un archivo. Es más flexible porque se le puede cambiar el nombre en el archivo que lo importa.

```js
export { Perfil };              // exportación con nombre
export default GaleriaDePerfiles; // exportación por defecto
```

La exportación por defecto también se puede indicar escribiendo `export default` delante de la definición del componente. En un archivo solo puede haber una exportación por defecto, que puede coexistir con múltiples exportaciones con nombre.

La importación se lleva a cabo con la misma sintaxis que la exportación, indicando además el nombre del archivo:

```jsx
import GaleriaDePerfiles from './GaleriaDePerfiles.js';

export default function App() {
  return (
    <GaleriaDePerfiles />
  );
}
```

### Escritura de etiquetas con JSX

**JavaScript XHTML (JSX)** es una extensión de JavaScript que permite escribir etiquetas del estilo de HTML dentro de JavaScript. El aumento de la interactividad de las aplicaciones web ha hecho que la lógica de la aplicación determinara su contenido, lo que sugería la conveniencia de unir en un mismo archivo ambos aspectos. De esa forma se mantienen juntos la lógica de representación gráfica y las etiquetas, y se asegura la sincronización de ambos aspectos cuando se modifica el archivo.

#### Diferencias entre JSX y HTML

JSX es muy similar a HTML pero introduce algunas reglas más estrictas:

- **Una función solo puede devolver un elemento.** Si se quiere devolver más de uno, hay que integrarlos dentro de una etiqueta `<div></div>` o un fragmento (`<></>`):

```jsx
<>
  <h1>Hedy Lamarr's Todos</h1>
  <img
    src="https://i.imgur.com/yXOvdOSs.jpg"
    alt="Hedy Lamarr"
    className="photo"
  />
  <ul>
    ...
  </ul>
</>
```

- **Todas las etiquetas tienen que estar cerradas.** En HTML hay etiquetas que no tienen que cerrarse (como `<img>`), pero en JSX todas deben estarlo. Por ejemplo, `<img>` se escribe `<img />`.

- **Notación camelCase para casi todos los atributos.** JSX se traduce a JavaScript y los atributos escritos en JSX se transforman en claves de objetos JavaScript. Por eso se usan nombres como `strokeWidth` en vez de `stroke-width`, o `className` en vez de `class` (que es una palabra reservada).

### JavaScript dentro de JSX

En JSX se pueden usar variables de JavaScript para darle valor a propiedades. Las variables JavaScript se pasan entre llaves (`{}`) dentro de JSX:

```jsx
export default function Avatar() {
  const avatar = "https://i.imgur.com/7vQD0fPs.jpg";
  const descripcion = "Gregorio Y. Zara";
  return (
    <img
      className='avatar'
      src={avatar}
      alt={descripcion}
    />
  );
}
```

Dentro de las llaves se puede usar cualquier expresión JavaScript, no solo valores de variables. Solo se pueden usar las llaves en JSX en dos lugares: como texto dentro del ámbito de etiquetas JSX (`<h1>Lista de {nombrePersona}</h1>`) o como valores de atributos justo después del símbolo `=` (`src={avatar}`).

También se pueden pasar objetos JavaScript. Como los objetos en JavaScript se escriben dentro de llaves, cuando se pasa como valor dentro de JSX hay que usar una notación de doble llave:

```jsx
export default function ListaTareas() {
  return (
    <ul style={{
      backgroundColor: 'black',
      color: 'pink'
    }}>
      <li>Mejorar el videófono.</li>
      <li>Preparar lección de aeronáutica</li>
    </ul>
  );
}
```

### Paso de propiedades a componentes

La forma de conseguir la versatilidad de los componentes es pasándoles **props**, el equivalente a los parámetros de las funciones. Para usar props en un componente hay que dar dos pasos. Primero, definir la lista de props en la definición de la función del componente (entre llaves, separadas por comas). Segundo, pasar los valores concretos en la invocación al componente:

```jsx
function Avatar({ persona, talla }) {
  return (
    <img
      className="avatar"
      src={getImageUrl(persona)}
      alt={persona.nombre}
      width={talla}
      height={talla}
    />
  );
}

export default function Perfil() {
  return (
    <div>
      <Avatar talla={100} persona={{ nombre: 'Katsuko Saruhashi', imageId: 'YfeOqp2' }} />
      <Avatar talla={80}  persona={{ nombre: 'Aklilu Lemma',      imageId: 'OKS67lh' }} />
    </div>
  );
}
```

Se pueden especificar valores por defecto para una prop:

```jsx
function Avatar({ persona, talla = 100 }) {
  // ...
}
```

Cuando un componente pasa todas sus props a otro componente anidado dentro de él, se puede usar una notación abreviada con el operador `...`:

```jsx
function Perfil(props) {
  return (
    <div className="card">
      <Avatar {...props} />
    </div>
  );
}
```

Igual que se pueden anidar etiquetas predefinidas, se pueden anidar componentes. El componente padre recibirá el contenido en una prop llamada `children`:

```jsx
function Tarjeta({ children }) {
  return (
    <div className="tarjeta">
      {children}
    </div>
  );
}

export default function Perfil3() {
  return (
    <Tarjeta>
      <Avatar talla={100} persona={{ nombre: 'Katsuko Saruhashi', imageId: 'YfeOqp2' }} />
    </Tarjeta>
  );
}
```

Las props que se pasan a un componente son **inmutables**: su valor no se puede cambiar respecto al que se recibe del componente padre. Si un componente tiene la necesidad de cambiar sus props, posiblemente lo que necesite sea definir un estado.

### Representación gráfica condicional

En JSX se pueden usar los operadores condicionales de JavaScript (`if`, `&&`, `?:`). Por ejemplo:

```jsx
function Elemento({ nombre, estaEmpaquetado }) {
  return <li className="elemento">{nombre} {estaEmpaquetado && '✅'}</li>;
}
```

Con el operador ternario:

```jsx
function Elemento({ nombre, estaEmpaquetado }) {
  return <li className="elemento">{estaEmpaquetado ? nombre + ' ✅' : nombre}</li>;
}
```

Si el operador `&&` devuelve `false`, React lo considera como un agujero en el árbol JSX (como `null` o `undefined`) y no muestra nada en su lugar. También se puede usar una variable JavaScript en el componente y cambiarle el valor en función de la condición:

```jsx
function Elemento({ nombre, estaEmpaquetado }) {
  let contenidoElemento = nombre;
  if (estaEmpaquetado) {
    contenidoElemento = contenidoElemento + ' ✅';
  }
  return <li className="elemento">{contenidoElemento}</li>;
}
```

### Representación gráfica de listas

La técnica habitual en React para definir listas es usar un array JavaScript y algún operador sobre arrays, como `map` o `filter`, para generar una lista de componentes:

```jsx
const gente = [
  { id: 1, nombre: 'Creola Katherine Johnson', profesion: 'matemática' },
  { id: 2, nombre: 'Mario José Molina',        profesion: 'químico'    },
  { id: 3, nombre: 'Mohammad Abdus Salam',     profesion: 'físico'     },
];

export default function List() {
  const quimicos = gente.filter(persona => persona.profesion === 'químico');
  const listaQuimicos = quimicos.map(persona =>
    <li key={persona.id}>{persona.nombre}</li>
  );
  return <ul>{listaQuimicos}</ul>;
}
```

Para resolver el error de que los elementos no tienen un identificador único hay que proporcionarles uno, dándole valor al atributo `key`. React usa esta clave única para saber cuándo se han reordenado, insertado o eliminado elementos de una lista. Las claves tienen que ser únicas entre los elementos de una misma lista pero se pueden reusar en otras listas. No se debe cambiar la clave de un elemento porque estropearía su funcionamiento.

Si se quiere mostrar más de un elemento DOM en cada elemento de la lista, es necesario usar el componente `Fragment` para encapsularlos:

```jsx
import { Fragment } from 'react';

export default function List() {
  const listItems = gente.map(persona =>
    <Fragment key={persona.id}>
      <h1>{persona.nombre}</h1>
      <p>{persona.profesion}</p>
    </Fragment>
  );
  return <div>{listItems}</div>;
}
```

### Mantener los componentes puros

En React todos los componentes se deben definir como **funciones puras**: funciones que solo calculan un valor y lo devuelven, sin modificar objetos o variables que existieran antes de su invocación, y que siempre devuelven el mismo valor para los mismos datos de entrada.

```jsx
function Receta({ cantidadComensales }) {
  return (
    <ol>
      <li>Hierve {cantidadComensales} tazas de agua.</li>
      <li>Añade {cantidadComensales} cucharadas de té.</li>
    </ol>
  );
}
```

La forma de conseguir efectos sin romper la pureza es modificar solo variables locales a la función, o usar manejadores de eventos —que no se ejecutan durante la representación gráfica y por tanto no tienen por qué ser puros—. Como último recurso se puede usar `useEffect`.

#### Modo estricto

React ofrece el **modo estricto** que ayuda a detectar componentes que no son puros. Para ello, durante el desarrollo llama dos veces a cada componente. El modo estricto no tiene incidencia en producción. Para usarlo, se envuelve el componente raíz en `<React.StrictMode>`:

```jsx
const root = ReactDOM.createRoot(document.querySelector('#root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

En React hay tres tipos de entradas que se pueden leer mientras se representan gráficamente los componentes: las **props**, el **estado** y el **contexto**. Los tres elementos se deberían tratar siempre como de solo lectura.

### El interfaz de usuario como un árbol de componentes

En React los componentes se definen todos al mismo nivel, pero en la construcción del interfaz de usuario se encuentran anidados unos dentro de otros, de manera similar a cómo están definidos los elementos HTML. Si en el cuerpo de un componente aparece otro componente, el componente externo es el padre del componente interno dentro del árbol de representación gráfica.

Otra relación que se puede representar en forma de árbol es la de las **dependencias entre módulos**. La forma de ambos árboles tendrá una forma parecida habitualmente, con modificaciones debidas a que haya múltiples componentes en un módulo, a módulos de los que se importen valores que no son componentes, o al paso de componentes como prop.

Cuando se construye una aplicación React para producción, una herramienta llamada **bundler** recorre el árbol de dependencias de módulos para saber qué módulos tiene que reunir para el producto final.

---

## 3. Interactividad

Las páginas web actuales tienen elementos que responden a acciones de los usuarios. Esas acciones se denominan **eventos**. La respuesta a esos eventos externos es la **interactividad**.

### Manejadores de eventos

La respuesta a un evento consiste en la ejecución de una función denominada **manejador del evento**. Para asociarla a un evento, se le asigna como atributo del componente HTML correspondiente. Estos atributos tienen un nombre que empieza por el prefijo `on` seguido del nombre del evento con la primera letra en mayúscula. Por convención, el nombre de las funciones manejadoras suele ser `manejaXxxx`:

```jsx
export default function Boton() {
  function manejaClick() {
    alert("¡Has pinchado con el ratón!");
  }
  return (
    <button onClick={manejaClick}>
      Pincha aquí
    </button>
  );
}
```

Las funciones también se pueden definir de manera integrada o usando la notación de flecha:

```jsx
<button onClick={() => alert("¡Has pinchado con el ratón!")}>
  Pincha aquí
</button>
```

> Es importante distinguir que el parámetro de la asignación del manejador `<button onClick={handleClick}>` es una **función**, no una invocación. La asignación `<button onClick={handleClick()}>` es incorrecta: el argumento sería el resultado de invocar `handleClick()` durante el dibujo del componente.

Como los manejadores están definidos dentro de un componente, tienen acceso a sus propiedades. Además, el componente padre puede pasar una función como prop para que cada instancia ejecute una acción distinta:

```jsx
function Boton({ onClick, children }) {
  return <button onClick={onClick}>{children}</button>;
}
```

#### Propagación de eventos

Los eventos se propagan hacia arriba a partir del elemento más interno. Para parar la propagación se usa el método `e.stopPropagation()` disponible en el parámetro implícito `e` (el evento):

```jsx
<button onClick={e => {
  e.stopPropagation();
  onClick();
}}>
```

Algunos eventos de navegador tienen un comportamiento por defecto asociado. Para evitarlo se invoca el método `e.preventDefault()`:

```jsx
<form onSubmit={e => {
  e.preventDefault();
  alert('Gracias por registrarte!');
}}>
```

### Estados: la memoria de los componentes

El valor de las variables locales de los componentes React no se mantiene entre representaciones gráficas. Para que las modificaciones se mantengan en el tiempo y provoquen una nueva representación gráfica, se usan **variables de estado** mediante el hook `useState`:

```jsx
import { useState } from 'react';

const [indice, setIndice] = useState(0);
```

El parámetro de `useState` es el valor inicial de la variable de estado. Por convención, el nombre de la función de actualización es el prefijo `set` seguido del nombre de la variable con la primera letra en mayúscula. La notación `[]` es la operación de desestructuración de arrays: `useState` devuelve un array con dos valores.

La variable de estado se actualiza invocando la función de actualización. Esa invocación cambia el valor de la variable y hace que React inicie una nueva representación gráfica:

```jsx
function handleClick() {
  setIndice((indice + 1) % listaEsculturas.length);
}
```

Un componente puede tener tantas variables de estado como necesite, cada una con su propio tipo. Los **Hooks** de React son funciones especiales que solo están disponibles mientras React está preparando la representación gráfica y cuyos nombres empiezan por `use`. Solo se pueden llamar en el nivel más externo de un componente, nunca dentro de sentencias condicionales, bucles u otras funciones anidadas.

Cada instancia de un componente tiene un **estado independiente** del estado de las otras instancias. Además, el estado es **privado**: un componente no conoce el estado de sus componentes internos. Si se quiere que varias instancias compartan estado, hay que moverlo a un componente en un nivel superior.

### Representación gráfica y consolidación

Hay dos motivos por los que se representa gráficamente un componente React:

- La representación gráfica inicial cuando se carga la página web.
- El estado del componente o de uno de sus ancestros ha sido actualizado.

La representación gráfica inicial se dispara invocando `createRoot` con el nodo DOM destino y llamando a `render` sobre él. En las siguientes representaciones, React calculará qué propiedades han cambiado desde la última modificación y aplicará los cambios mínimos necesarios en el árbol DOM.

Después de que React consolide los cambios en el DOM, el navegador refrescará el contenido de la pantalla con los nuevos valores.

### Estado como instantánea

Las variables de estado no son como las variables locales: permanecen en el entorno de React, fuera de las funciones. Cuando React ejecuta la función de un componente, le proporciona una **instantánea del estado** para esa representación gráfica en particular.

El cambio de estado solo cambia el valor de la variable para la siguiente representación gráfica. Si la función de actualización se llama varias veces en la misma representación gráfica, el valor final corresponde solo a la última invocación —todas usan el valor de la variable al inicio de la representación gráfica actual—:

```jsx
<button onClick={() => {
  setNumero(numero + 1);
  setNumero(numero + 1);
  setNumero(numero + 1); // el contador solo avanza 1, no 3
}}>+3</button>
```

### Colas de actualizaciones de estado

React espera hasta que todo el código de los manejadores de eventos se haya ejecutado antes de procesar las actualizaciones de estado (**batching**). Aunque es poco habitual, para actualizar varias veces la misma variable antes de la próxima representación gráfica se puede pasar una función como parámetro a la función de actualización:

```jsx
<button onClick={() => {
  setNumero(n => n + 1);
  setNumero(n => n + 1);
  setNumero(n => n + 1); // ahora sí avanza 3
}}>+3</button>
```

React encola la función para que se procese después de que se haya ejecutado todo el otro código del manejador del evento y, durante la siguiente representación gráfica, recorre la cola y aplica cada actualización en orden.

### Actualización de objetos en estado

En un estado se pueden guardar objetos. Sin embargo, estos objetos se deberían tratar como si fueran **inmutables**: no cambiar los valores de sus campos, sino sustituirlos por nuevos objetos con los nuevos valores a través de la función de actualización:

```jsx
setPosicion({ x: 5, y: 0 });
```

Para evitar repetir todos los campos se puede usar el operador `...` (spread sobre objetos):

```jsx
setPersona({
  ...persona,
  nombrePropio: e.target.value
});
```

La copia hecha con `...` no es profunda, por lo que hay que repetirla si se quiere actualizar un campo interno:

```jsx
setPersona({
  ...persona,
  direccion: {
    ...persona.direccion,
    codigoPostal: 29071
  }
});
```

### Actualización de arrays en estado

Los arrays son un tipo particular de objetos, por lo que se deben tratar igual: considerarlos inmutables y cambiar su valor creando un nuevo array. Hay que evitar métodos que modifican el array directamente (`push`, `pop`, `splice`, `reverse`, `sort`) y usar en su lugar métodos que crean nuevos arrays (`concat`, `[...arr]`, `filter`, `slice`, `map`).

Para **añadir** un elemento al final:

```jsx
setArtistas([...artistas, { id: nextId++, nombre: nombre }]);
```

Para **eliminar** un elemento:

```jsx
setArtistas(artistas.filter(a => a.id !== artist.id));
```

Para **modificar** un elemento:

```jsx
const proximasFormas = formas.map(forma => {
  if (forma.type === 'cuadrado') {
    return forma;
  } else {
    return { ...forma, y: forma.y + 50 };
  }
});
setFormas(proximasFormas);
```

Para **insertar** en una posición intermedia, se combina `slice` con el operador `...`:

```jsx
const sigArtistas = [
  ...artistas.slice(0, insertaEn),
  { id: sigId++, nombre: nombre },
  ...artistas.slice(insertaEn),
];
setArtistas(sigArtistas);
```

> La copia de arrays con `...` es una copia superficial. Si los elementos del array son objetos, tanto el array original como el nuevo comparten referencias a los mismos objetos. Para evitar ese efecto lateral hay que crear un nuevo objeto y asignárselo a la posición correspondiente en el nuevo array.

---

## 4. Gestión del estado en React

### Definición declarativa de las respuestas a eventos

En React no se especifican las acciones de manera imperativa, sino que se define de manera **declarativa** cuál es el aspecto que se quiere mostrar en cada momento. Se recomiendan cinco pasos para definir la respuesta a los eventos:

1. **Identificar** cuáles son los posibles estados visuales de los componentes.
2. **Determinar** cuáles son los eventos que provocan los cambios de estados.
3. **Representar** el estado en memoria usando `useState`.
4. **Eliminar** las variables de estado no esenciales.
5. **Conectar** los manejadores de eventos para establecer los estados.

#### Identificar los posibles estados visuales

El primer paso es identificar cuáles son los posibles estados visuales. Para un formulario podría ser: `vacio`, `escribiendo`, `enviando`, `exito`, `error`. Es conveniente definir bocetos gráficos directamente en React, escribiendo solo la parte estática:

```jsx
export default function Form({ estado = 'vacio' }) {
  if (estado === 'exito') {
    return <h1>¡Correcto!</h1>;
  }
  return (
    <form>
      <textarea disabled={estado === 'enviando'} />
      <button disabled={estado === 'vacio' || estado === 'enviando'}>Enviar</button>
      {estado === 'error' && <p className="Error">Respuesta incorrecta.</p>}
    </form>
  );
}
```

Si un componente tiene muchos posibles estados visuales, es conveniente mostrarlos todos juntos:

```jsx
const statuses = ['vacio', 'escribiendo', 'enviando', 'exito', 'error'];

export default function App() {
  return (
    <>
      {statuses.map(estado => (
        <section key={estado}>
          <h4>Form ({estado}):</h4>
          <Form estado={estado} />
        </section>
      ))}
    </>
  );
}
```

#### Eliminar variables de estado no esenciales

Hay que evitar la duplicación del contenido del estado. Algunas preguntas ayudan a reducir el número de variables:

- ¿Puede el conjunto de variables de estado causar alguna paradoja?
- ¿Puede derivarse el valor de una variable de estado a partir del valor de otra?

Por ejemplo, si `estaVacio` puede derivarse de `respuesta.length === 0`, se puede eliminar. Si `estaEscribiendo` y `estaEnviando` son incompatibles, mejor sustituirlas por una sola variable `estado` con los posibles valores `'escribiendo'`, `'enviando'` y `'exito'`.

### Selección de la estructura del estado

Los siguientes principios ayudan a conseguir un buen estado:

- **Agrupar estados relacionados**: si dos o más variables de estado se actualizan siempre a la vez, es conveniente agruparlas en una única variable.
- **Evitar contradicciones**: hay que evitar que diferentes variables de estado puedan contradecirse entre sí. Usar una sola variable con los posibles valores como alternativa a varias variables de tipo lógico.
- **Evitar estados redundantes**: si el valor de una variable se puede inferir a partir de otras, se debe eliminar.
- **Evitar duplicación**: hay que evitar datos duplicados en diferentes variables de estado o en objetos anidados.
- **Evitar estados excesivamente anidados**: es más fácil mantener estados con menos niveles de anidamiento. Se puede **aplanar el estado** usando identificadores en vez de objetos anidados.

### Compartir estado entre componentes

Cuando se quiere compartir el estado entre varias instancias de un mismo tipo, la solución habitual es **subir el estado** al objeto común más cercano en el árbol de elementos del IU. Para ello hacen falta varias acciones:

1. Eliminar el estado de los componentes inferiores.
2. Añadir una prop que permita al componente inferior acceder al estado.
3. Añadir el estado al componente padre.
4. Pasar el valor de la nueva variable de estado a los componentes inferiores como prop.
5. Pasar una función de manejo de evento del componente superior a los inferiores para actualizar el estado común.

```jsx
function Panel({ titulo, estaActivo, onActivar, children }) {
  return (
    <section>
      <h3>{titulo}</h3>
      {estaActivo ? <p>{children}</p> : <button onClick={onActivar}>Mostrar</button>}
    </section>
  );
}

export default function Acordeon() {
  const [indiceActivo, setIndiceActivo] = useState(0);
  return (
    <>
      <Panel titulo="Sobre"     estaActivo={indiceActivo === 0} onActivar={() => setIndiceActivo(0)}>...</Panel>
      <Panel titulo="Etimología" estaActivo={indiceActivo === 1} onActivar={() => setIndiceActivo(1)}>...</Panel>
    </>
  );
}
```

Un componente cuyo estado es gestionado por él mismo se llama **descontrolado**. Un componente cuyo estado se pasa desde fuera como prop se llama **controlado**. Los componentes descontrolados suelen ser más reutilizables, pero los controlados suelen ser más flexibles.

### Preservar y reiniciar el estado

React almacena el estado de un componente mientras el componente siga en el árbol de IU. Si el componente desaparece, React borra su estado. Si reaparece, su estado será el inicial.

Más concretamente, React preserva el estado de un componente mientras se muestre en la **misma posición** en el árbol de IU con el mismo tipo de componente. Si el componente se elimina del árbol o se muestra otro tipo de componente en la misma posición, React descarta su estado.

Para forzar que React reinicie el estado de un componente que está en la misma posición del árbol IU, se puede añadir un identificador con el atributo `key`:

```jsx
{esJugadorA ? (
  <Contador key="Teresa" persona="Teresa" />
) : (
  <Contador key="Sabino" persona="Sabino" />
)}
```

El uso de `key` para diferenciar instancias del mismo tipo es especialmente útil para reiniciar el estado de formularios, como en aplicaciones de chat donde al cambiar de destinatario se debe borrar el contenido del campo de texto.

### Extracción de la lógica de estado a un reductor

Un **reductor** (`reducer`) es una función de React que agrupa toda la lógica de estado de un componente. Para cambiar la forma de tratar el estado y usar un reductor, son necesarios tres pasos:

#### 1. Cambiar las llamadas a las funciones de estado por acciones de despacho

```jsx
function handleAddTarea(texto) {
  dispatch({ type: 'anadir', id: nextId++, texto: texto });
}
function handleChangeTarea(tarea) {
  dispatch({ type: 'cambiar', tarea: tarea });
}
function handleDeleteTarea(tareaId) {
  dispatch({ type: 'eliminar', tareaId: tareaId });
}
```

#### 2. Escribir una función reductora

```jsx
function tareasReducer(tareas, accion) {
  switch (accion.type) {
    case 'anadir': {
      return [...tareas, { id: accion.id, texto: accion.texto, hecho: false }];
    }
    case 'cambiar': {
      return tareas.map(t => t.id === accion.tarea.id ? accion.tarea : t);
    }
    case 'eliminar': {
      return tareas.filter(t => t.id !== accion.tareaId);
    }
    default: {
      throw Error('Acción desconocida: ' + accion.type);
    }
  }
}
```

Es recomendable incluir el código de cada rama de la sentencia `switch` dentro de llaves y acabar con `return` para evitar que la ejecución continúe por la siguiente rama.

#### 3. Usar el reductor desde el componente

```jsx
import { useReducer } from 'react';

const [tareas, dispatch] = useReducer(tareasReducer, tareasIniciales);
```

`useReducer` tiene dos parámetros: la función reductora y el estado inicial. Devuelve la variable de estado y la función `dispatch`.

### Paso de datos en profundidad con contexto

Cuando un dato pasa en forma de prop por múltiples componentes que solo lo usan para pasarlo hacia abajo, se produce la situación llamada **prop drilling**. React ofrece el mecanismo de **contexto** por el cual un componente interno puede acceder directamente a datos situados en componentes superiores, incluso habiendo varios intermedios.

Para trabajar con contextos hay que realizar tres acciones:

#### 1. Crear el contexto

```jsx
import { createContext } from 'react';

export const ContextoNivel = createContext(1);
```

#### 2. Usar el contexto en el componente que necesita el dato

```jsx
import { useContext } from 'react';
import { ContextoNivel } from './ContextoNivel.js';

export default function Cabecera({ children }) {
  const nivel = useContext(ContextoNivel);
  // ...
}
```

#### 3. Proporcionar el contexto desde el componente que especifica el dato

```jsx
import { ContextoNivel } from './ContextoNivel.js';

function Seccion({ nivel, children }) {
  return (
    <section>
      <ContextoNivel value={nivel}>
        {children}
      </ContextoNivel>
    </section>
  );
}
```

También se puede pasar el contexto de manera recursiva. Cada componente `<Seccion>` anidado lee el nivel del contexto y lo incrementa para sus hijos:

```jsx
function Seccion({ children }) {
  const nivel = useContext(ContextoNivel);
  return (
    <section>
      <ContextoNivel value={nivel + 1}>
        {children}
      </ContextoNivel>
    </section>
  );
}
```

El uso de contextos puede recordar al de variables globales y debe hacerse siempre de manera justificada. Situaciones habituales en las que se usa el contexto: tema visual, cuenta de usuario actual, enrutado y gestión del estado. El contexto no solo permite valores estáticos: si se pasa un valor distinto en la siguiente visualización, React actualizará todos los componentes que lo lean.

### Escalado con reductor y contexto

Para evitar el prop drilling con variables de estado, se puede combinar el estado con el contexto. El proceso se divide en tres pasos:

#### 1. Creación del contexto

```jsx
import { createContext } from 'react';

export const ContextoTareas = createContext(null);
export const ContextoDespachoTareas = createContext(null);
```

#### 2. Colocación del estado y la función de despacho en el contexto

```jsx
return (
  <ContextoTareas value={tareas}>
    <ContextoDespachoTareas value={dispatch}>
      ...
    </ContextoDespachoTareas>
  </ContextoTareas>
);
```

#### 3. Uso del contexto en cualquier parte del árbol IU

```jsx
function ListaTareas() {
  const tareas = useContext(ContextoTareas);
  // ...
}
function AnadirTarea() {
  const dispatch = useContext(ContextoDespachoTareas);
  // ...
}
```

Es una práctica recomendada mover el reductor y el contexto a un mismo archivo, exportando un componente proveedor con `children` como prop:

```jsx
export function ProveedorTareas({ children }) {
  const [tareas, dispatch] = useReducer(tareasReducer, tareasIniciales);
  return (
    <ContextoTareas value={tareas}>
      <ContextoDespachoTareas value={dispatch}>
        {children}
      </ContextoDespachoTareas>
    </ContextoTareas>
  );
}
```

---

## 5. Trampillas de escape

En ciertas ocasiones, algunos componentes React necesitan controlar y sincronizarse con sistemas externos a React. React ofrece **trampillas de escape** (*escape hatches*) para estos casos. La mayor parte del código de React no debería depender de estas características.

### Referencias (Refs)

Cuando un componente quiere recordar información sin volver a dibujar el árbol IU, debe usar **referencias** en vez de estados:

```jsx
import { useRef } from 'react';

const ref = useRef(0);
```

El hook `useRef` toma un valor como parámetro (el valor inicial) y devuelve un objeto con un atributo `current` en el que se almacena el valor referenciado. El código React puede acceder a `ref.current` tanto para leer su valor como para modificarlo.

A diferencia de los estados, la modificación de una referencia **no hace que React vuelva a dibujar el componente**. Como los estados, el valor de las referencias se mantiene entre las diferentes veces que React dibuja el árbol IU.

Las referencias se pueden usar para almacenar el identificador de intervalos, objetos que no son necesarios para calcular el JSX, o para manipular elementos del DOM. No se deben usar durante el dibujo del componente.

Se pueden combinar referencias y estados en un mismo componente. Por ejemplo, en un cronómetro: los tiempos (necesarios para la visualización) van en estado, y el identificador del intervalo (no necesario para la visualización) va en una referencia:

```jsx
import { useState, useRef } from 'react';

export default function Cronometro() {
  const [tiempoInicial, setTiempoInicial] = useState(null);
  const [ahora, setAhora] = useState(null);
  const intervaloRef = useRef(null);

  function handleIniciar() {
    setTiempoInicial(Date.now());
    setAhora(Date.now());
    intervaloRef.current = setInterval(() => {
      setAhora(Date.now());
    }, 10);
  }

  function handleParar() {
    clearInterval(intervaloRef.current);
  }

  let segundosTranscurridos = 0;
  if (tiempoInicial != null && ahora != null) {
    segundosTranscurridos = (ahora - tiempoInicial) / 1000;
  }

  return (
    <>
      <h1>Tiempo transcurrido: {segundosTranscurridos.toFixed(1)}</h1>
      <button onClick={handleIniciar}>Iniciar</button>
      <button onClick={handleParar}>Parar</button>
    </>
  );
}
```

### Manipulación del DOM con referencias

En ocasiones hay que manipular el DOM directamente para, por ejemplo, poner el foco en un componente, hacer scroll hasta él o medir su tamaño. Para ello se importa `useRef`, se define una referencia y se inicializa la propiedad `ref` del componente con ella:

```jsx
import { useRef } from 'react';

export default function FocoElementoDOM() {
  const inputRef = useRef(null);

  function handleClick() {
    inputRef.current.focus();
  }

  return (
    <>
      <input ref={inputRef} />
      <button onClick={handleClick}>Foco sobre el input</button>
    </>
  );
}
```

Cuando React crea el nodo DOM para ese elemento, pone una referencia a él en `ref.current`. A partir de ese momento se puede usar sobre él las API de navegador predefinidas.

El manejo de elementos DOM con referencias debería incluir solo acciones no destructivas (poner el foco, hacer scroll). Manipular el DOM manualmente puede entrar en conflicto con los cambios que hace React.

### Sincronización con efectos

Los **efectos** permiten ejecutar código después de dibujar los componentes para que puedan sincronizarse con sistemas externos a React (una API del navegador, una biblioteca de terceros, un servidor remoto...). A diferencia de los manejadores de eventos, los efectos se ejecutan como consecuencia del dibujo del árbol IU, no de un evento concreto.

Un efecto se describe en tres pasos:

#### 1. Declaración del efecto

Para declarar un efecto hay que importar el hook `useEffect` y usarlo en el inicio de la función principal del componente:

```jsx
import { useEffect } from 'react';

function miComponente() {
  useEffect(() => {
    // código que se ejecuta después del dibujo
  });
  return <div />;
}
```

#### 2. Especificación de las dependencias del efecto

Por defecto, los efectos se ejecutan después de cada dibujo del componente. Para limitar las ocasiones en que se ejecuta, se añade una lista de dependencias. El efecto solo se ejecutará cuando el valor de alguno de los elementos haya cambiado:

```jsx
useEffect(() => {
  if (estaReproduciendo) {
    ref.current.play();
  } else {
    ref.current.pause();
  }
}, [estaReproduciendo]); // solo se ejecuta cuando cambia estaReproduciendo
```

Si la lista de dependencias está vacía (`[]`), el efecto solo se ejecuta la primera vez que se monta el componente.

La lista de dependencias no se puede escoger libremente: tiene que incluir todos los valores reactivos (props y variables de estado) de los que dependa el efecto.

#### 3. Especificación de una operación de limpieza

En ciertas ocasiones es necesario ejecutar una operación de limpieza antes de volver a ejecutar el efecto (o al desmontar el componente). La función de limpieza se especifica como resultado de la función `useEffect`:

```jsx
useEffect(() => {
  const conexion = crearConexion();
  conexion.conectar();
  return () => conexion.desconectar();
}, []);
```

Cuando un componente se vuelve a dibujar con valores de dependencias distintos, primero se ejecuta la función de limpieza con los valores antiguos y después se ejecuta la acción del efecto con los valores nuevos. Cuando el componente desaparezca del DOM se volverá a ejecutar la función de limpieza.

Otras situaciones en las que se necesita función de limpieza: suscripción a eventos, control de animaciones, descarga de datos del servidor (para ignorar respuestas caducadas):

```jsx
useEffect(() => {
  let ignore = false;
  fetchResults(consulta, pagina).then(json => {
    if (!ignore) setResultados(json);
  });
  return () => { ignore = true; };
}, [consulta, pagina]);
```

### Cuándo no usar efectos

Los efectos se deben usar para sincronizarse con elementos externos. Las siguientes situaciones son casos en los que usar efectos no es la mejor solución:

- **Cuando se actualiza el estado basándose en props u otras variables de estado.** Mejor calcular el valor como variable local durante el dibujo del componente.
- **Cuando hay cálculos caros repetidos.** Usar el hook `useMemo` para que el cálculo no se repita cuando sus dependencias no han cambiado.
- **Reiniciar el estado cuando cambia una prop.** En vez de modificar el estado en un efecto, forzar a que React reinicie el estado pasando una `key` diferente al componente.
- **Compartir lógica entre manejadores de eventos.** Definir una función en el componente con el código compartido e invocarla desde ambos manejadores.
- **Enviar una petición POST como respuesta a un evento.** Usar un manejador de eventos, no un efecto (los efectos se usan cuando la acción es consecuencia de mostrar el componente, no de un evento).
- **Cadenas de cálculos.** Calcular todo el estado posible durante el dibujo o en el manejador del evento, para evitar múltiples redibujos.
- **Notificar a los componentes padres cambios de estado.** Actualizar el estado de ambos componentes dentro de la misma manejadora de evento, en vez de usar un efecto.
- **Pasar datos a los componentes padres.** El flujo natural en React es de padres a hijos. Si ambos necesitan los datos, que sea el padre quien los descargue y los pase hacia abajo.
- **Suscribirse a almacenes externos.** Usar el hook integrado `useSyncExternalStore` en vez de sincronizar manualmente con un efecto:

```jsx
import { useSyncExternalStore } from 'react';

function suscribir(callback) {
  window.addEventListener('online', callback);
  window.addEventListener('offline', callback);
  return () => {
    window.removeEventListener('online', callback);
    window.removeEventListener('offline', callback);
  };
}

function useOnlineStatus() {
  return useSyncExternalStore(
    suscribir,
    () => navigator.onLine,
    () => true
  );
}
```

- **Descargar datos.** Aunque es habitual usar efectos para descargar datos, la solución es más robusta si se extrae la lógica a un hook particular que maneje la condición de carrera con una función de limpieza:

```jsx
function useData(url) {
  const [datos, setDatos] = useState(null);
  useEffect(() => {
    let ignore = false;
    fetch(url)
      .then(response => response.json())
      .then(json => {
        if (!ignore) setDatos(json);
      });
    return () => { ignore = true; };
  }, [url]);
  return datos;
}
```

En general, cuando hay que escribir efectos, hay que pensar siempre si se puede extraer parte de la funcionalidad a un hook particular más declarativo y orientado a una función. Cuantos menos efectos desnudos haya en un componente, más fácil será mantener la aplicación.