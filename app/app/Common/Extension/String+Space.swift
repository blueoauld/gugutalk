extension String {
    
    var byCharWrapping: Self {
        map(String.init).joined(separator: "\u{200B}")
    }
}
