# Guía de Referencia para el Agente IA — Desarrollo Web para Clientes: HTML

> **INSTRUCCIÓN CRÍTICA**: Este agente tiene **terminantemente prohibido** usar, sugerir, generar o hacer referencia a cualquier elemento HTML, atributo, tecnología o práctica que **no esté explícitamente recogida en esta guía**. Todo lo aquí descrito proviene directamente del temario oficial de la asignatura. Cualquier duda sobre si algo pertenece o no al temario debe resolverse de forma conservadora: **si no está en esta guía, no se usa**.

---

## Índice

1. [Contexto tecnológico: servidor vs cliente](#1-contexto-tecnológico-servidor-vs-cliente)
2. [Fundamentos de HTML5](#2-fundamentos-de-html5)
3. [Estructura básica de un documento HTML5](#3-estructura-básica-de-un-documento-html5)
4. [Metadatos y cabecera](#4-metadatos-y-cabecera)
5. [Referencias a caracteres especiales](#5-referencias-a-caracteres-especiales)
6. [Comentarios](#6-comentarios)
7. [Texto: encabezados y párrafos](#7-texto-encabezados-y-párrafos)
8. [Énfasis, importancia y otros elementos de texto](#8-énfasis-importancia-y-otros-elementos-de-texto)
9. [Características avanzadas de texto](#9-características-avanzadas-de-texto)
10. [Líneas y bloques especiales](#10-líneas-y-bloques-especiales)
11. [Listas](#11-listas)
12. [Estructura y semántica](#12-estructura-y-semántica)
13. [Div y Span](#13-div-y-span)
14. [Enlaces](#14-enlaces)
15. [Imágenes](#15-imágenes)
16. [Vídeo y Audio](#16-vídeo-y-audio)
17. [Tablas](#17-tablas)
18. [Marcos incrustados (iframe)](#18-marcos-incrustados-iframe)
19. [Formularios](#19-formularios)
20. [Validación de formularios con HTML](#20-validación-de-formularios-con-html)
21. [Buenas prácticas y restricciones](#21-buenas-prácticas-y-restricciones)

---

## 1. Contexto tecnológico: servidor vs cliente

El temario incluye una introducción a las tecnologías de servidor para contextualizar el rol del HTML. **El agente NO debe generar código de servidor**, pero sí debe conocer y poder explicar este contexto.

- El navegador únicamente interpreta **HTML, CSS y JavaScript**. Nunca recibe ni interpreta PHP, Python ni Ruby.
- Tecnologías de servidor mencionadas en el temario (solo a nivel conceptual): **PHP**, **Python con Flask**, **Python con Django**, **Ruby on Rails**.
- El servidor ejecuta la lógica, genera HTML dinámicamente y envía únicamente HTML/CSS/JS al navegador.
- El proceso es: `Navegador → petición GET → Servidor → ejecución de lógica → HTML generado → Navegador`.

---

## 2. Fundamentos de HTML5

### Qué es HTML

HTML (*HyperText Markup Language*) es un **lenguaje de etiquetas** que define el **contenido, la estructura y la semántica** de las páginas web. La versión del temario es **HTML5**.

### Sintaxis de un elemento

```html
<etiqueta atributo1="valor1" atributo2="valor2">
    contenido del elemento
</etiqueta>
```

- Los elementos tienen **etiqueta de apertura** y **etiqueta de cierre**.
- Los atributos se escriben en la etiqueta de apertura con la forma `nombre="valor"`.
- Los elementos pueden **anidarse**, siempre cerrando el más interno antes que el externo.
- Algunos elementos son **void elements** (sin etiqueta de cierre): `<br>`, `<img>`, `<input>`, `<meta>`, `<link>`, `<hr>`.

---

## 3. Estructura básica de un documento HTML5

La plantilla básica del temario es la siguiente:

```html
<!DOCTYPE html>
<html lang="es-ES">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi página de prueba</title>
    <!-- <link rel="stylesheet" href="estilos.css"> -->
    <!-- <script src="script.js" defer></script> -->
  </head>
  <body>
    <p>Esta es mi página de prueba</p>
  </body>
</html>
```

### Reglas obligatorias de estructura

| Elemento | Obligatoriedad | Descripción |
|---|---|---|
| `<!DOCTYPE html>` | **Obligatorio** | Declaración del tipo de documento |
| `<html>` | Obligatorio | Envuelve todo el contenido |
| `<head>` | Obligatorio | Cabecera con metadatos (no se renderiza) |
| `<title>` | **Obligatorio** (salvo en iframes) | Título visible en la pestaña del navegador |
| `<body>` | Obligatorio | Todo el contenido visible de la página |

- El atributo `lang` en `<html>` **se debe** especificar. Formato: `lang="es-ES"`.
- Se puede usar `lang` también en elementos individuales para cambiar el idioma de un fragmento: `<p lang="en-US">`.

---

## 4. Metadatos y cabecera

Los elementos del `<head>` **no se visualizan** en el navegador (excepción: `<title>`).

### `<meta>`

```html
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="author" content="Nombre Apellido">
<meta name="description" content="Descripción del contenido de la página.">
```

### `<link>` — Importar CSS

Siempre debe estar en la **cabecera**.

```html
<link rel="stylesheet" href="estilos.css">
```

- Atributo `rel="stylesheet"`: indica que es una hoja de estilos.
- Atributo `href`: ruta del archivo CSS.

### `<script>` — Importar JavaScript

Debe incluir el atributo `defer` para que los elementos HTML se carguen antes de ejecutar el JavaScript.

```html
<script src="archivo.js" defer></script>
```

- Atributo `src`: ruta del archivo JavaScript.
- Atributo `defer`: **siempre se debe usar** para evitar errores de referencia a elementos no cargados aún.

> **Nota del temario**: aunque es posible incluir CSS con `<style>` y JavaScript con `<script>` directamente en el HTML, en páginas grandes se aconseja usar archivos externos referenciados con `<link>` y `<script src="...">`.

---

## 5. Referencias a caracteres especiales

Los caracteres especiales de HTML no se pueden escribir directamente en el contenido. Se usan referencias de entidad:

| Referencia | Carácter |
|---|---|
| `&lt;` | `<` |
| `&gt;` | `>` |
| `&amp;` | `&` |
| `&quot;` | `"` |
| `&apos;` | `'` |
| `&aacute;` | `á` |
| `&eacute;` | `é` |
| `&iacute;` | `í` |
| `&oacute;` | `ó` |
| `&uacute;` | `ú` |
| `&ntilde;` | `ñ` |
| `&iquest;` | `¿` |

> Con codificación `UTF-8` (que es la estándar en HTML5), las vocales acentuadas y la eñe pueden escribirse directamente.

---

## 6. Comentarios

```html
<!-- Esto es un comentario -->
```

Los comentarios no se renderizan en el navegador.

---

## 7. Texto: encabezados y párrafos

### Encabezados

Existen seis niveles de encabezado. Se usan **jerárquicamente**.

```html
<h1>Encabezado 1</h1>
<h2>Encabezado 2</h2>
<h3>Encabezado 3</h3>
<h4>Encabezado 4</h4>
<h5>Encabezado 5</h5>
<h6>Encabezado 6</h6>
```

**Buenas prácticas de encabezados:**
- Solo debe haber **un único `<h1>`** por página, que corresponde al título principal.
- Cada encabezado debe usarse dentro de un encabezado del nivel inmediatamente superior.

### Párrafos

```html
<p>Ejemplo de párrafo sencillo.</p>
```

- Todo el contenido textual debe estar dentro de párrafos `<p>`.
- Por defecto están justificados a la izquierda (modificable con CSS).
- El atributo `align` de HTML 4.01 **ha desaparecido en HTML5**. No se debe usar.

---

## 8. Énfasis, importancia y otros elementos de texto

```html
<strong>texto importante</strong>   <!-- negrita semántica -->
<em>texto enfatizado</em>           <!-- cursiva semántica -->
<b>texto destacable</b>             <!-- negrita tipográfica -->
<i>extranjerismo o término técnico</i>  <!-- cursiva tipográfica -->
<u>nombre propio o error ortográfico</u>  <!-- subrayado (uso muy restringido) -->
<mark>resultado de búsqueda</mark>  <!-- texto resaltado -->
```

**Restricciones de uso:**
- `<i>`: solo para palabras en otros idiomas o clasificación taxonómica.
- `<b>`: solo para palabras clave o nombres de producto.
- `<u>`: uso **muy restringido** porque históricamente se asocia a hiperenlaces.
- `<strong>` y `<em>` son preferibles a `<b>` e `<i>` cuando se quiere dar significado semántico.

---

## 9. Características avanzadas de texto

### Citas de bloque

```html
<blockquote cite="https://fuente-original.com">
  <p>Texto de la cita extendida.</p>
</blockquote>
```

### Citas en línea

```html
<q cite="https://fuente-original.com">cita corta en línea</q>
```

> El atributo `cite` no se muestra en pantalla sin CSS o JavaScript. Para mostrarlo con solo HTML, se añade un enlace `<a>` y un elemento `<cite>` con el nombre de la fuente.

### Abreviaturas

```html
<abbr title="HyperText Markup Language">HTML</abbr>
```

### Subíndices y superíndices

```html
<sup>superíndice</sup>
<sub>subíndice</sub>
```

Ejemplo: `H<sub>2</sub>O`, `1<sup>o</sup>`

### Código de programación

```html
<pre><code>
const x = 1;
</code></pre>
```

- `<code>`: para fragmentos de código genérico.
- `<pre>`: respeta todos los espacios en blanco. Se combina con `<code>` para bloques de código.

### Fechas y horas

```html
<time datetime="2016-01-20">20 de enero 2016</time>
```

El atributo `datetime` permite formatos como `"19:30"`, `"2016-01-20T19:30+01:00"`, etc.

---

## 10. Líneas y bloques especiales

```html
<br>    <!-- Retorno de carro (void element) -->
<hr>    <!-- Línea horizontal (void element) -->
<pre>texto con espacios y formato fijo</pre>
```

---

## 11. Listas

### Lista ordenada

```html
<ol>
  <li>Primer elemento</li>
  <li>Segundo elemento</li>
  <li>Tercer elemento</li>
</ol>
```

### Lista no ordenada

```html
<ul>
  <li>Elemento sin orden</li>
  <li>Otro elemento</li>
</ul>
```

### Lista de definición

```html
<dl>
  <dt>Término</dt>
  <dd>Definición del término</dd>
  <dt>Otro término</dt>
  <dd>Su definición</dd>
</dl>
```

- `<dl>`: contenedor de la lista de definición.
- `<dt>`: término a definir.
- `<dd>`: definición del término (con sangrado).

### Listas anidadas

Las listas pueden anidarse entre sí (ordenadas dentro de no ordenadas, y viceversa):

```html
<ol>
  <li>Elemento con sublista:
    <ol>
      <li>Subelemento 1</li>
      <li>Subelemento 2</li>
    </ol>
  </li>
  <li>Otro elemento</li>
</ol>
```

---

## 12. Estructura y semántica

### Elementos semánticos de bloque (HTML5)

Desde HTML5 existen etiquetas semánticas que deben usarse en lugar de `<div>` genéricos cuando sea aplicable. Mejoran la accesibilidad (lectores de pantalla) y el SEO.

| Elemento | Uso |
|---|---|
| `<header>` | Cabecera general de la página o sección |
| `<nav>` | Barra de navegación con enlaces principales |
| `<main>` | Contenido principal de la página |
| `<aside>` | Contenido relacionado pero secundario |
| `<footer>` | Pie de página (contacto, copyright…) |
| `<article>` | Contenido autónomo y reutilizable |
| `<section>` | Sección temática dentro del contenido principal |

**Estructura típica de una página web:**

```html
<body>
  <header>...</header>
  <nav>...</nav>
  <main>
    <article>
      <section>...</section>
    </article>
    <aside>...</aside>
  </main>
  <footer>...</footer>
</body>
```

> **Importante**: el uso de estos elementos no aplica estilos especiales por defecto. Su disposición visual se logra con CSS, no con HTML.

---

## 13. Div y Span

```html
<div class="especial">
  <h3>Cabecera</h3>
  <p>Párrafo dentro del div</p>
</div>

<p>Mi madre tiene ojos <span class="azul">azules</span> y pelo <span class="amarillo">rubio</span>.</p>
```

- `<div>`: contenedor de bloque genérico. Se usa para agrupar elementos y aplicarles estilos CSS.
- `<span>`: contenedor de línea genérico. Se usa para aplicar estilos a fragmentos de texto en línea.
- Habitualmente se definen con el atributo `class` para asociarles estilos en CSS.

---

## 14. Enlaces

### Enlace básico

```html
<a href="pagina.html">Texto del enlace</a>
```

### Tipos de rutas

```html
<!-- Ruta relativa (RECOMENDADA para recursos propios) -->
<a href="CV.html">Mi currículum</a>

<!-- Ruta absoluta a otro servidor -->
<a href="https://www.google.com">Enlace a Google</a>

<!-- NUNCA se debe usar ruta absoluta para recursos propios del servidor -->
<!-- MAL: <a href="http://www.miweb.com/docs/CV.html">...</a> -->
```

> **Buena práctica**: para recursos del propio servidor, usar siempre **rutas relativas**. Las rutas absolutas propias fallan si cambia el dominio o directorio.

### Enlace a fragmento de la misma página

```html
<!-- Marca de destino -->
<a id="seccion-final"></a>

<!-- Enlace al fragmento -->
<a href="#seccion-final">Ir al final</a>
```

### Enlace a fragmento de otra página

```html
<a href="https://www.ejemplo.es/pagina.html#seccion-final">Ir al final de otra página</a>
```

### Atributo `target`

| Valor | Comportamiento |
|---|---|
| `_blank` | Abre en ventana o pestaña nueva |
| `_self` | Abre en el mismo marco (comportamiento por defecto) |
| `_parent` | Abre en el marco padre |
| `_top` | Abre en el marco principal |
| `framename` | Abre en una ventana/pestaña con ese nombre |

### Imagen como enlace

```html
<a href="pagina.html"><img src="imagen.jpg" alt="descripción"></a>
```

---

## 15. Imágenes

```html
<img src="imagen.jpg" alt="Descripción de la imagen">
<img src="imagen.jpg" alt="Descripción" width="500">
<img src="https://url-externa.com/imagen.png" alt="Logo">
```

### Con figura y pie de foto

```html
<figure>
  <img src="images/foto.jpeg" alt="Descripción" width="800" height="600">
  <figcaption>Fig. 1. Descripción de la imagen.</figcaption>
</figure>
```

### Atributos de `<img>`

| Atributo | Descripción |
|---|---|
| `src` | Ruta de la imagen (relativa, absoluta o URL externa) |
| `alt` | Texto alternativo (obligatorio por accesibilidad) |
| `width` | Ancho en píxeles CSS |
| `height` | Alto en píxeles CSS |

**Buenas prácticas de imágenes:**
- El atributo `alt` es fundamental para la **accesibilidad**.
- Si se especifican `width` y `height`, deben **respetar la relación de aspecto** para evitar distorsiones.
- En general, es mejor especificar **solo uno** de los dos valores de dimensión y dejar que el navegador calcule el otro proporcionalmente.
- `src` puede ser una URL a cualquier imagen en Internet, o una ruta relativa o absoluta al servidor propio.

---

## 16. Vídeo y Audio

### Vídeo básico

```html
<video src="video.webm">
  <p>Su navegador no es capaz de reproducir vídeo HTML.</p>
</video>
```

### Vídeo con múltiples fuentes (recomendado)

```html
<video controls width="400" height="400">
  <source src="video.mp4" type="video/mp4">
  <source src="video.webm" type="video/webm">
  <p>Su navegador no soporta este vídeo. <a href="video.mp4">Descárgalo aquí</a>.</p>
</video>
```

### Atributos del elemento `<video>`

| Atributo | Descripción |
|---|---|
| `src` | Fuente del vídeo |
| `controls` | Muestra controles básicos (play, pausa, volumen) |
| `width` | Ancho del reproductor |
| `height` | Alto del reproductor |
| `autoplay` | Inicia automáticamente al cargar. **Se recomienda NO activar** |
| `loop` | Repite el vídeo al finalizar. **Se recomienda NO activar** |

### Formatos de vídeo soportados

- **WebM** y **MP4** son los soportados por los principales navegadores.

### Audio

```html
<audio controls>
  <source src="audio.mp3" type="audio/mp3">
  <source src="audio.ogg" type="audio/ogg">
  <p>Su navegador no soporta este archivo de audio. <a href="audio.mp3">Descárgalo aquí</a>.</p>
</audio>
```

### Formatos de audio soportados

- **MP3**, **OGG** y **WAV** son los más habituales.

---

## 17. Tablas

### Estructura básica

```html
<table>
  <tr>
    <td>Celda 1</td>
    <td>Celda 2</td>
  </tr>
  <tr>
    <td>Celda 3</td>
    <td>Celda 4</td>
  </tr>
</table>
```

### Tabla completa con cabecera, cuerpo y pie

```html
<table>
  <caption>Título o descripción de la tabla</caption>
  <thead>
    <tr>
      <th>Columna 1</th>
      <th>Columna 2</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Dato 1</td>
      <td>Dato 2</td>
    </tr>
  </tbody>
  <tfoot>
    <tr>
      <td>Pie 1</td>
      <td>Pie 2</td>
    </tr>
  </tfoot>
</table>
```

### Unión de celdas

```html
<!-- Unir columnas -->
<td colspan="2">Celda que ocupa 2 columnas</td>

<!-- Unir filas -->
<td rowspan="2">Celda que ocupa 2 filas</td>
```

### Elementos de tabla permitidos

| Elemento | Descripción |
|---|---|
| `<table>` | Contenedor de la tabla |
| `<caption>` | Descripción o título de la tabla |
| `<thead>` | Sección de cabecera (opcional, semántica) |
| `<tbody>` | Sección de cuerpo |
| `<tfoot>` | Sección de pie (opcional, semántica) |
| `<tr>` | Fila de la tabla |
| `<th>` | Celda de cabecera de columna |
| `<td>` | Celda de datos |

> `<thead>` y `<tfoot>` son opcionales. El número de columnas en cabecera y pie debe ser igual. No es obligatorio usar `<tbody>` si solo hay cuerpo.

---

## 18. Marcos incrustados (iframe)

```html
<iframe
  id="miIframe"
  title="Ejemplo de iframe"
  width="300"
  height="200"
  src="https://es.wikipedia.org/wiki/HTML">
</iframe>
```

### Atributos de `<iframe>`

| Atributo | Descripción |
|---|---|
| `src` | URL de la página externa a incrustar |
| `title` | Título descriptivo (importante para accesibilidad) |
| `width` | Ancho del marco |
| `height` | Alto del marco |
| `id` | Identificador del elemento |

> Los marcos incrustados representan un **contexto de navegación anidado** con su propio árbol de elementos, independiente de la página que lo contiene.

---

## 19. Formularios

Los formularios recogen datos del usuario y los envían al servidor. HTML y HTTP solo se encargan del envío; la lógica de procesamiento es responsabilidad del servidor.

### Elemento `<form>`

```html
<form action="https://servidor.es/procesar" method="post">
  <!-- elementos del formulario -->
</form>
```

### Atributos de `<form>`

| Atributo | Descripción |
|---|---|
| `action` | URL del programa servidor que procesará los datos |
| `method` | Método HTTP: `GET` o `POST` |
| `enctype` | Codificación. Usar `text/plain` solo si `action` es `mailto:` |

**Diferencia entre GET y POST:**
- `GET`: añade los datos a la URL como cadena de consulta (`?campo=valor&campo2=valor2`). Los datos son visibles en la URL.
- `POST`: envía los datos en el cuerpo de la petición HTTP. Los datos no son visibles en la URL. **Es el método más usado y obligatorio cuando se envía a correo electrónico.**

### Elemento `<label>`

Las etiquetas son fundamentales: explican el propósito de cada entrada y mejoran la **accesibilidad**.

```html
<!-- Asociación explícita mediante for/name -->
<label for="nombre">Nombre completo:</label>
<input type="text" name="nombre" id="nombre">

<!-- Asociación implícita anidando el input dentro del label -->
<label>Nombre completo:
  <input type="text" name="nombre">
</label>
```

### Elemento `<input>`

El principal elemento para recoger información. El atributo `type` define su comportamiento.

#### Tipos de `<input>` del temario

| `type` | Descripción |
|---|---|
| `text` | Campo de texto de una línea |
| `password` | Texto oculto (muestra `*`). Requiere HTTPS para envío seguro |
| `checkbox` | Casilla de verificación (puede seleccionarse varias) |
| `radio` | Botón de opción (excluyente dentro del mismo `name`) |
| `submit` | Botón para enviar el formulario |
| `reset` | Botón para restablecer valores por defecto |
| `hidden` | Campo oculto, no visible para el usuario |
| `image` | Botón de imagen para enviar el formulario |
| `button` | Botón sin funcionalidad por defecto |
| `file` | Selector de archivos |
| `color` | Selector de color |
| `email` | Campo de correo electrónico (valida el formato) |
| `tel` | Campo de teléfono (activa teclado numérico en móviles) |
| `url` | Campo de URL (valida el formato) |
| `number` | Campo numérico con controles de incremento |
| `range` | Deslizador para valores en un rango |
| `date` | Selector de fecha |
| `time` | Selector de hora |
| `search` | Campo de búsqueda |

#### Atributos comunes de `<input>`

| Atributo | Descripción |
|---|---|
| `type` | Tipo de entrada |
| `name` | Nombre que se envía al servidor como clave del par nombre/valor |
| `id` | Identificador para asociar con `<label>` y CSS |
| `value` | Valor por defecto o valor enviado |
| `required` | El campo no puede estar vacío al enviar |
| `disabled` | Desactiva el elemento |
| `maxlength` | Número máximo de caracteres (en `text`) |
| `size` | Tamaño visual del campo en caracteres |
| `checked` | Elemento seleccionado por defecto (en `radio` y `checkbox`) |
| `min` | Valor mínimo (en `number`, `range`, `date`, `time`) |
| `max` | Valor máximo (en `number`, `range`, `date`, `time`) |
| `step` | Intervalo de incremento (en `number`, `range`, `date`) |
| `pattern` | Expresión regular para validar el valor |
| `accept` | Tipos de archivo permitidos (en `file`) |
| `multiple` | Permite seleccionar varios archivos (en `file`) |
| `src` | Fuente de imagen (en `image`) |
| `alt` | Texto alternativo (en `image`) |
| `readonly` | Solo lectura, no modificable por el usuario |
| `list` | Asocia el input a un `<datalist>` |

#### Ejemplos de tipos de entrada

```html
<!-- Texto -->
<input type="text" name="nombre" maxlength="50" size="30">

<!-- Email -->
<input type="email" name="email" id="email">

<!-- Teléfono -->
<input type="tel" name="tel" id="tel">

<!-- URL -->
<input type="url" name="url" id="url">

<!-- Número -->
<input type="number" name="edad" min="1" max="120" step="1">

<!-- Rango (deslizador) -->
<input type="range" name="precio" min="500" max="5000" step="50" value="2500">

<!-- Fecha -->
<input type="date" name="fecha" min="2025-06-01" max="2025-08-31">

<!-- Hora -->
<input type="time" name="hora">

<!-- Archivo -->
<input type="file" name="archivo" accept="image/*" multiple>

<!-- Password -->
<input type="password" name="contrasena">

<!-- Oculto -->
<input type="hidden" name="token" value="abc123">

<!-- Imagen como botón de envío -->
<input type="image" src="boton.png" alt="Enviar" width="80" height="30">

<!-- Submit y reset -->
<input type="submit" value="Enviar">
<input type="reset" value="Borrar">

<!-- Radio (mismo name = grupo excluyente) -->
<input type="radio" name="opcion" value="a" checked> Opción A
<input type="radio" name="opcion" value="b"> Opción B

<!-- Checkbox -->
<input type="checkbox" name="acepta" value="si" checked> Acepto los términos
```

### Elemento `<button>`

```html
<button>Texto del botón</button>
```

- Fuera de un formulario: sin funcionalidad por defecto, requiere JavaScript.
- Dentro de un formulario: por defecto envía (`submit`) el formulario.

### Elemento `<textarea>`

```html
<textarea name="comentario" cols="20" rows="10">Texto por defecto</textarea>
```

| Atributo | Descripción |
|---|---|
| `name` | Nombre del campo para el servidor |
| `cols` | Número de columnas visibles |
| `rows` | Número de filas visibles |
| `disabled` | Deshabilita el área de texto |
| `readonly` | Solo lectura |
| `maxlength` | Número máximo de caracteres |

### Elemento `<select>` — Lista desplegable

```html
<select name="opcion" size="1">
  <option value="uno">Uno</option>
  <option value="dos" selected>Dos</option>
  <option value="tres">Tres</option>
</select>
```

| Atributo de `<select>` | Descripción |
|---|---|
| `name` | Nombre para el servidor |
| `size` | Número de opciones visibles (`1` = desplegable, `>1` = caja) |
| `multiple` | Permite selección múltiple |
| `disabled` | Desactiva la lista |

| Atributo de `<option>` | Descripción |
|---|---|
| `value` | Valor enviado al servidor |
| `selected` | Opción seleccionada por defecto |

#### Agrupación de opciones con `<optgroup>`

```html
<select name="grupos">
  <optgroup label="Frutas">
    <option>Naranja</option>
    <option selected>Chirimoya</option>
  </optgroup>
  <optgroup label="Verduras">
    <option>Lechuga</option>
    <option>Apio</option>
  </optgroup>
</select>
```

### Elemento `<datalist>`

Proporciona sugerencias a un `<input>` sin forzar al usuario a escoger solo de la lista.

```html
<input type="text" name="fruta" list="lista-frutas">
<datalist id="lista-frutas">
  <option>Naranja</option>
  <option>Manzana</option>
  <option>Fresa</option>
</datalist>
```

### Elementos `<fieldset>` y `<legend>`

Para agrupar campos relacionados dentro de un formulario:

```html
<form>
  <fieldset>
    <legend>Tamaño del plato</legend>
    <input type="radio" name="size" id="size_1" value="tapa">
    <label for="size_1">Tapa</label>
    <input type="radio" name="size" id="size_2" value="ración">
    <label for="size_2">Ración</label>
  </fieldset>
</form>
```

- `<fieldset disabled>`: deshabilita todos los elementos internos.
- `<legend>`: texto descriptivo superpuesto al borde del `<fieldset>`.

### Elemento `<output>`

Elemento especial de tipo `<label>` para mostrar el valor de un control (como un `<input type="range">`). Su actualización requiere JavaScript.

```html
<input type="range" name="precio" id="precio" min="0" max="1000">
<output class="precio-output" for="precio"></output>
```

---

## 20. Validación de formularios con HTML

El temario cubre **únicamente la validación del lado del cliente mediante atributos HTML**. La validación en el servidor siempre es obligatoria por seguridad, pero no es parte de este temario.

### Atributos de validación HTML

| Atributo | Descripción |
|---|---|
| `required` | El campo no puede estar vacío |
| `min` / `max` | Rango de valores válidos |
| `step` | Precisión del incremento |
| `type` | Limita el tipo de datos (`number`, `email`, `url`, etc.) |
| `pattern` | Expresión regular para valores válidos |
| `maxlength` | Longitud máxima del texto |

### Ejemplo de formulario con validación

```html
<form>
  <p>Los campos marcados con (*) son obligatorios.</p>
  <fieldset>
    <legend>¿Tiene permiso de conducir? *</legend>
    <input type="radio" required name="conductor" id="r1" value="si">
    <label for="r1">Sí</label>
    <input type="radio" required name="conductor" id="r2" value="no">
    <label for="r2">No</label>
  </fieldset>
  <p>
    <label for="edad">Edad</label>
    <input type="number" min="12" max="120" step="1" id="edad" name="edad">
  </p>
  <p>
    <label for="fruta">¿Fruta favorita? *</label>
    <input
      type="text"
      id="fruta"
      name="fruta"
      list="lista-frutas"
      required
      pattern="[Pp]latano|[Mm]anzana|[Ff]resa">
    <datalist id="lista-frutas">
      <option>Platano</option>
      <option>Manzana</option>
      <option>Fresa</option>
    </datalist>
  </p>
  <p>
    <label for="email">Correo electrónico</label>
    <input type="email" id="email" name="email">
  </p>
  <p>
    <label for="msg">Mensaje breve</label>
    <textarea id="msg" name="msg" maxlength="140" rows="5"></textarea>
  </p>
  <p>
    <button>Enviar</button>
  </p>
</form>
```

---

## 21. Buenas prácticas y restricciones

### Prácticas obligatorias (según el temario)

- Incluir siempre `<!DOCTYPE html>` al inicio del documento.
- Especificar siempre el atributo `lang` en `<html>`.
- Usar siempre `<meta charset="utf-8">` en la cabecera.
- Incluir siempre `<title>` en la cabecera.
- Usar `defer` en las etiquetas `<script>` para cargar JavaScript.
- Usar rutas **relativas** para recursos propios del servidor.
- Incluir siempre el atributo `alt` en las imágenes.
- Usar elementos semánticos (`<header>`, `<nav>`, `<main>`, etc.) en lugar de `<div>` genéricos cuando proceda.
- Usar `<label>` asociado a cada elemento de entrada en formularios.
- Los campos de contraseña deben enviarse siempre por **HTTPS**, nunca por HTTP.
- Asociar `<label>` a `<input>` mediante `for`/`id` o anidamiento.
- Usar `<fieldset>` y `<legend>` para agrupar campos relacionados en formularios extensos.

### Prácticas desaconsejadas o prohibidas (según el temario)

- **No usar** el atributo `align` en párrafos (pertenece a HTML 4.01, eliminado en HTML5). La alineación se hace con CSS.
- **Evitar** las rutas absolutas propias del servidor en atributos `href` o `src`.
- **No activar** `autoplay` ni `loop` en vídeos (el temario lo desaconseja explícitamente).
- **No usar** `<u>` para texto sin una justificación clara (por confusión con hiperenlaces).
- **No mezclar** presentación con estructura: el estilo se hace en CSS, no con atributos HTML de presentación.

### Lo que este agente NO puede usar

- Cualquier elemento HTML no mencionado en este documento.
- Atributos HTML no descritos explícitamente en este temario.
- CSS (el temario lo menciona como tecnología vinculada, pero sus reglas y propiedades no forman parte del contenido dado).
- JavaScript (ídem: se menciona su vinculación pero no se estudia su sintaxis ni uso en este temario).
- Frameworks, librerías o herramientas externas de ningún tipo.
- Elementos o atributos de versiones anteriores a HTML5 que hayan sido eliminados (como `align` en párrafos).

---

*Guía generada a partir del temario oficial de la asignatura de Desarrollo Web para Clientes — HTML.*