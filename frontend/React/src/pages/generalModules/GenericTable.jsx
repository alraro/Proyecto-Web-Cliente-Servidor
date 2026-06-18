import { motion, AnimatePresence } from 'framer-motion';

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

function renderTableRows(data, headers, editRowFunction, deleteRowFunction, isLoading) {
	return (
		<>
			<tbody>
				<AnimatePresence >
					{isLoading ? (
						<motion.tr
							key="loading-state"
							initial={{ opacity: 0 }}
							animate={{ opacity: 1 }}
							exit={{ opacity: 0 }}
						>
							<td colSpan="100%" className='loading-message'>
								<div className="spinner-loader">Cargando datos...</div>
							</td>
						</motion.tr>
					) : data.length > 0 ? (
						data.map((row, rowIndex) => (
						<motion.tr
							key={row.id}
							initial={{ opacity: 0, height: 0 }}
							animate={{ opacity: 1, height: 'auto' }}
							exit={{ opacity: 0, height: 0 }}
							transition={{ duration: 0.2, ease: 'easeInOut' }}
							layout
							style={{ overflow: 'hidden' }}
						>
							{Object.keys(headers).map((header, colIndex) => (
								<td key={colIndex}>{row[header.toLowerCase().replace(' ', '_')]}</td>
							))}
							{editRowFunction && <td><button className="btn btn-edit btn-sm" onClick={() => editRowFunction(row)}>Editar</button></td>}
							{deleteRowFunction && <td><button className="btn btn-danger btn-sm" onClick={() => deleteRowFunction(row)}>Eliminar</button></td>}
						</motion.tr>
						))
					) : (
						<motion.tr
							key="no-data-message"
							initial={{ opacity: 0 }}
							animate={{ opacity: 1 }}
							exit={{ opacity: 0 }}
							transition={{ duration: 0.2 }}
						>
							<td colSpan="100%">No existen datos disponibles</td>
						</motion.tr>
					)}
				</AnimatePresence>
			</tbody>
		</>
	)
}

function GenericTable({
	title,
	headers,
	data = [],
	editRowFunction=null,
	deleteRowFunction=null,
	addRowFunction=null,
	itemName="Fila",
	onChangeSearch=null,
	filterCondition=null,
	isLoading=false
}) {

	if (!headers) return (<div>No headers provided</div>);

	if (filterCondition) data = data.filter(filterCondition);

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
				{onChangeSearch && (
					<div className="filters-bar">
						<input type="text" placeholder="Buscar..." className="search-field" onChange={(e) => onChangeSearch(e.target.value)}/>
					</div>
				)}
				<div className="table-wrap">
					<table>
						{renderTableHeaders(headers, editRowFunction, deleteRowFunction)}
						{renderTableRows(data, headers, editRowFunction, deleteRowFunction, isLoading)}
					</table>
				</div>
			</div>
		</>
  	);
}

export default GenericTable;