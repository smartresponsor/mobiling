import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ListProjectsQuery {
    let searchTerm: String?
    let stateCode: String?
    let cursor: String?
    let limit: Int
}
