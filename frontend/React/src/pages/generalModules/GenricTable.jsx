
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

function GenericTable({ title, headers, data }) {

	title = title || "";
	if (!headers) return (<div>No headers provided</div>);

	return (
		<div className="table-wrapper">
			<h2>{title}</h2>
			<table>
				{renderTableHeaders(headers)}
				{renderTableRows(data, headers)}
			</table>
		</div>
  	);
}

export default GenericTable;