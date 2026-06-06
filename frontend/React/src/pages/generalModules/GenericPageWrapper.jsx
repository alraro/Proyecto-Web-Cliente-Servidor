import GenericHeader from "./GenericHeader"

function GenericPageWrapper({ children }) {

    return (
        <main className="page-wrapper">
            <GenericHeader />
            {children}
        </main>
    )
}

export default GenericPageWrapper