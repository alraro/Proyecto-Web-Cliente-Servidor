import GenericHeader from "./GenericHeader"

function GenericPageWrapper({ headerUsername, children }) {
    return (
        <main className="page-wrapper">
            <GenericHeader username={headerUsername} />
            {children}
        </main>
    )
}

export default GenericPageWrapper