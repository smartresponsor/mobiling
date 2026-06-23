import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ProjectListingScreenContract {
    let title: String
    let rows: [ProjectSummary]
    let nextCursor: String?
}
