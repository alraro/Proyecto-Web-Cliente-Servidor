import GenericHeader from "./GenericHeader"

function GenericPageWrapper({ children }) {

    return (
        <>
            <GenericHeader />
            <main className="page-wrapper">
                {children}
            </main>
        </>
    )
}

export default GenericPageWrapper
