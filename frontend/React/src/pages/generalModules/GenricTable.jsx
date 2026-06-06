
function renderTableHeaders(headers) {
	return (
		<>
			<thead>
				<tr>
					{Object.keys(headers).map((header, index) => (
						<th key={index}>{headers[header]}</th>
					))}
				</tr>
			</thead>
		</>
	)
}

function renderTableRows(data, headers) {
	return (
		<>
			<tbody>
				{data.map((row, rowIndex) => (
					<tr key={rowIndex}>
						{Object.keys(headers).map((header, colIndex) => (
							<td key={colIndex}>{row[header.toLowerCase().replace(' ', '_')]}</td>
						))}
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

function GenericTable({ headers, data }) {

	if (!headers) return (<div>No headers provided</div>);

	return (
		<div className="table-wrapper">
			<table>
				{renderTableHeaders(headers)}
				{data && data.length > 0 && renderTableRows(data, headers)}
				{!data || data.length === 0 && renderNoData()}
			</table>
		</div>
  	);
}

export default GenericTable;