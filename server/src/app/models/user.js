var mongoose = require('mongoose');
var bcrypt = require('bcrypt-nodejs');

var UserSchema = new mongoose.Schema({
  username: { type: String, unique: true, required: true }, // Username
  password: { type: String, required: true } // Password (will be hashed)
});

// Encrypt the password before each user.save() call
UserSchema.pre('save', function(callback) {
  var user = this;

  // Don't save the password if it has not changed
  if (!user.isModified('password')) return callback();

  // Encrypt the password and save it
  bcrypt.genSalt(5, function(err, salt) {
    if (err) return callback(err);

    bcrypt.hash(user.password, salt, null, function(err, hash) {
      if (err) return callback(err);
      user.password = hash;
      callback();
    });
  });
});

// Verify the password
UserSchema.methods.verifyPassword = function(password, cb) {
  bcrypt.compare(password, this.password, function(err, isMatch) {
    if (err) return cb(err);
    cb(null, isMatch);
  });
};


module.exports = mongoose.model('User', UserSchema);
