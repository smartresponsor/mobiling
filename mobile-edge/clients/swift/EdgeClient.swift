
import Foundation

public final class EdgeClient {
    let base: String
    let bearer: String?
    public init(base:String, bearer:String?=nil){
        self.base = base.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        self.bearer = bearer
    }
    private func req(path:String, method:String="GET", body:Data?=nil, headers:[String:String]=[:], done:@escaping (Result<Data,Error>)->Void){
        var r = URLRequest(url: URL(string: base + path)!)
        r.httpMethod = method
        headers.forEach { r.setValue($1, forHTTPHeaderField: $0) }
        if let b = body { r.httpBody = b; r.setValue("application/json", forHTTPHeaderField: "Content-Type") }
        if let b = bearer { r.setValue(b, forHTTPHeaderField: "Authorization") }
        URLSession.shared.dataTask(with: r){ data, resp, err in
            if let err = err { return done(.failure(err)) }
            guard let http = resp as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
                return done(.failure(NSError(domain:"http", code: (resp as? HTTPURLResponse)?.statusCode ?? 500)))
            }
            done(.success(data ?? Data()))
        }.resume()
    }
    public func config(done:@escaping (Result<Data,Error>)->Void){ req(path:"/mobile/config", done: done) }
    public func entitlement(done:@escaping (Result<Data,Error>)->Void){ req(path:"/mobile/entitlement", done: done) }
    public func pushRegister(deviceId:String, token:String, platform:String?=nil, done:@escaping (Result<Void,Error>)->Void){
        let body = try! JSONSerialization.data(withJSONObject: ["deviceId":deviceId,"token":token,"platform":platform as Any])
        req(path:"/mobile/push/register", method:"POST", body: body){ r in switch r{ case .success: done(.success(())); case .failure(let e): done(.failure(e)) } }
    }
    public func receiptVerify(idem:String, payload:[String:Any], done:@escaping (Result<Data,Error>)->Void){
        var h = [String:String](); h["Idempotency-Key"] = idem
        let body = try! JSONSerialization.data(withJSONObject: payload)
        req(path:"/mobile/receipt/verify", method:"POST", body: body, headers:h, done: done)
    }
}
