

function GenericTable({ title, headers, data }) {
  return (
    <div className="table-wrapper">
        <table>
            <thead>
                <tr>
                    {Object.keys(headers).map((header, index) => (
                        <th key={index}>{headers[header]}</th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {data.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                        {Object.keys(headers).map((header, colIndex) => (
                            <td key={colIndex}>{row[header.toLowerCase().replace(' ', '_')]}</td>
                        ))}
                    </tr>
                ))}
            </tbody>
        </table>
    </div>
  );
}

export default GenericTable;