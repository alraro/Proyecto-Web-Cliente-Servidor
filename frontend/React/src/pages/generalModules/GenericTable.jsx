
function renderTableHeaders(headers, editRowFunction, deleteRowFunction) {
	return (
		<>
			<thead>
				<tr>
					{Object.keys(headers).map((header, index) => (
						<th key={index}>{headers[header]}</th>
					))}
					{editRowFunction && <th></th>}
					{deleteRowFunction && <th></th>}
				</tr>
			</thead>
		</>
	)
}

function renderTableRows(data, headers, editRowFunction, deleteRowFunction) {
	return (
		<>
			<tbody>
				{data.map((row, rowIndex) => (
					<tr key={rowIndex}>
						{Object.keys(headers).map((header, colIndex) => (
							<td key={colIndex}>{row[header.toLowerCase().replace(' ', '_')]}</td>
						))}
						{editRowFunction && <td><button className="btn btn-edit btn-sm" onClick={() => editRowFunction(row)}>Editar</button></td>}
						{deleteRowFunction && <td><button className="btn btn-danger btn-sm" onClick={() => deleteRowFunction(row)}>Eliminar</button></td>}
					</tr>
				))}
			</tbody>
		</>
	)
}

function renderNoData() {
	return (
		<tbody>
			<tr>
				<td colSpan="100%">No existen datos disponibles</td>
			</tr>
		</tbody>
	)
}

function GenericTable({ title, headers, data, editRowFunction=null, deleteRowFunction=null, addRowFunction=null, itemName="Fila" }) {

	if (!headers) return (<div>No headers provided</div>);

	return (
		<>
			<div className="card">
				<div className="card-header">
					<h2>{title}</h2>
					<div className="card-actions">
						{addRowFunction && (
							<button className="btn btn-primary" onClick={addRowFunction}>
								Agregar {itemName + 's'}
							</button>
						)}
					</div>
				</div>
				<div className="table-wrap">

					<table>
						{renderTableHeaders(headers, editRowFunction, deleteRowFunction)}
						{data && data.length > 0 && renderTableRows(data, headers, editRowFunction, deleteRowFunction)}
						{!data || data.length === 0 && renderNoData()}
					</table>
				</div>
			</div>
		</>
  	);
}

export default GenericTable;